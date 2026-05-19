package com.hisrah.accounting.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hisrah.accounting.dto.request.JournalEntryActionRequest;
import com.hisrah.accounting.dto.request.JournalEntryLineRequest;
import com.hisrah.accounting.dto.request.JournalEntryUpsertRequest;
import com.hisrah.accounting.dto.response.JournalEntryLineResponse;
import com.hisrah.accounting.dto.response.JournalEntryResponse;
import com.hisrah.accounting.entity.Account;
import com.hisrah.accounting.entity.JournalEntry;
import com.hisrah.accounting.entity.JournalEntryLine;
import com.hisrah.accounting.entity.JournalEntryStatus;
import com.hisrah.accounting.exception.BadRequestException;
import com.hisrah.accounting.exception.ConflictException;
import com.hisrah.accounting.exception.ForbiddenException;
import com.hisrah.accounting.exception.NotFoundException;
import com.hisrah.accounting.exception.UnprocessableEntityException;
import com.hisrah.accounting.repository.AccountRepository;
import com.hisrah.accounting.repository.JournalEntryRepository;

@Service
public class JournalEntryService {

	private static final DateTimeFormatter REFERENCE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final Set<LocalDate> LOCKED_PERIOD_DATES = Set.of(
		LocalDate.of(2025, 12, 31),
		LocalDate.of(2026, 3, 31)
	);

	private final JournalEntryRepository journalEntryRepository;
	private final AccountRepository accountRepository;

	public JournalEntryService(
		JournalEntryRepository journalEntryRepository,
		AccountRepository accountRepository
	) {
		this.journalEntryRepository = journalEntryRepository;
		this.accountRepository = accountRepository;
	}

	@Transactional
	public JournalEntryResponse create(JournalEntryUpsertRequest request) {
		validateUpsertRequest(request);

		JournalEntry entry = new JournalEntry();
		entry.setReferenceNo(generateReferenceNo(request.getEntryDate()));
		entry.setEntryDate(request.getEntryDate());
		entry.setDescription(request.getDescription().trim());
		entry.setCreatedBy(request.getCreatedBy().trim());
		entry.setStatus(JournalEntryStatus.DRAFT);
		entry.replaceLines(buildLines(request.getLines()));

		return toResponse(journalEntryRepository.save(entry));
	}

	@Transactional(readOnly = true)
	public List<JournalEntryResponse> list() {
		return journalEntryRepository.findAllByOrderByIdDesc().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public JournalEntryResponse getById(Long id) {
		return toResponse(getEntryById(id));
	}

	@Transactional
	public JournalEntryResponse update(Long id, JournalEntryUpsertRequest request) {
		validateUpsertRequest(request);
		JournalEntry entry = getEntryById(id);

		if (entry.getStatus() == JournalEntryStatus.APPROVED) {
			throw new ConflictException("approved journal entry cannot be modified");
		}
		if (entry.getStatus() != JournalEntryStatus.DRAFT && entry.getStatus() != JournalEntryStatus.REJECTED) {
			throw new ConflictException("only DRAFT or REJECTED entries can be edited");
		}

		entry.setEntryDate(request.getEntryDate());
		entry.setDescription(request.getDescription().trim());
		entry.setCreatedBy(request.getCreatedBy().trim());
		entry.setStatus(JournalEntryStatus.DRAFT);
		entry.setRejectedBy(null);
		entry.setRejectionReason(null);
		entry.replaceLines(buildLines(request.getLines()));

		return toResponse(journalEntryRepository.save(entry));
	}

	@Transactional
	public void delete(Long id) {
		JournalEntry entry = getEntryById(id);
		if (entry.getStatus() != JournalEntryStatus.DRAFT) {
			throw new ConflictException("only DRAFT entries can be deleted");
		}
		journalEntryRepository.delete(entry);
	}

	@Transactional
	public JournalEntryResponse submit(Long id, JournalEntryActionRequest request) {
		JournalEntry entry = getEntryById(id);
		if (entry.getStatus() != JournalEntryStatus.DRAFT) {
			throw new ConflictException("only DRAFT entries can be submitted");
		}
		validateEntryDate(entry.getEntryDate());
		entry.setStatus(JournalEntryStatus.PENDING_APPROVAL);
		return toResponse(journalEntryRepository.save(entry));
	}

	@Transactional
	public JournalEntryResponse approve(Long id, JournalEntryActionRequest request) {
		JournalEntry entry = getEntryById(id);
		if (entry.getStatus() != JournalEntryStatus.PENDING_APPROVAL) {
			throw new ConflictException("only PENDING_APPROVAL entries can be approved");
		}

		String approver = normalizeActor(request.getActionBy(), "actionBy is required");
		if (approver.equalsIgnoreCase(entry.getCreatedBy())) {
			throw new ForbiddenException("creator cannot approve their own journal entry");
		}

		entry.setStatus(JournalEntryStatus.APPROVED);
		entry.setApprovedBy(approver);
		for (JournalEntryLine line : entry.getLines()) {
			line.setPosted(true);
		}

		return toResponse(journalEntryRepository.save(entry));
	}

	@Transactional
	public JournalEntryResponse reject(Long id, JournalEntryActionRequest request) {
		JournalEntry entry = getEntryById(id);
		if (entry.getStatus() != JournalEntryStatus.PENDING_APPROVAL) {
			throw new ConflictException("only PENDING_APPROVAL entries can be rejected");
		}

		String rejectedBy = normalizeActor(request.getActionBy(), "actionBy is required");
		if (!StringUtils.hasText(request.getReason())) {
			throw new BadRequestException("rejection reason is required");
		}

		entry.setStatus(JournalEntryStatus.REJECTED);
		entry.setRejectedBy(rejectedBy);
		entry.setRejectionReason(request.getReason().trim());

		return toResponse(journalEntryRepository.save(entry));
	}

	@Transactional
	public JournalEntryResponse reverse(Long id, JournalEntryActionRequest request) {
		JournalEntry original = getEntryById(id);
		if (original.getStatus() != JournalEntryStatus.APPROVED) {
			throw new ConflictException("only APPROVED entries can be reversed");
		}

		original.setStatus(JournalEntryStatus.REVERSED);
		journalEntryRepository.save(original);

		JournalEntry reversal = new JournalEntry();
		reversal.setReferenceNo(generateReferenceNo(LocalDate.now()));
		reversal.setEntryDate(LocalDate.now());
		reversal.setDescription("Reversal of " + original.getReferenceNo());
		reversal.setCreatedBy(normalizeActor(request.getActionBy(), "actionBy is required"));
		reversal.setApprovedBy(reversal.getCreatedBy());
		reversal.setStatus(JournalEntryStatus.APPROVED);
		reversal.setReversalOf(original);

		List<JournalEntryLine> reversalLines = new ArrayList<>();
		for (JournalEntryLine line : original.getLines()) {
			JournalEntryLine reversalLine = new JournalEntryLine();
			reversalLine.setAccount(line.getAccount());
			reversalLine.setDebitAmount(line.getCreditAmount());
			reversalLine.setCreditAmount(line.getDebitAmount());
			reversalLine.setPosted(true);
			reversalLines.add(reversalLine);
		}
		reversal.replaceLines(reversalLines);

		return toResponse(journalEntryRepository.save(reversal));
	}

	private String generateReferenceNo(LocalDate entryDate) {
		String datePart = entryDate.format(REFERENCE_DATE_FORMAT);
		String prefix = "JE-" + datePart + "-";
		long count = journalEntryRepository.countByReferenceNoStartingWith(prefix);
		return prefix + String.format("%04d", count + 1);
	}

	private void validateUpsertRequest(JournalEntryUpsertRequest request) {
		validateEntryDate(request.getEntryDate());

		if (request.getLines().size() < 2) {
			throw new UnprocessableEntityException("journal entry must contain at least 2 lines");
		}

		BigDecimal totalDebit = BigDecimal.ZERO;
		BigDecimal totalCredit = BigDecimal.ZERO;
		for (JournalEntryLineRequest line : request.getLines()) {
			validateLine(line);
			totalDebit = totalDebit.add(line.getDebitAmount());
			totalCredit = totalCredit.add(line.getCreditAmount());
		}

		if (totalDebit.compareTo(totalCredit) != 0) {
			throw new UnprocessableEntityException("journal entry is not balanced: total debit must equal total credit");
		}
	}

	private void validateLine(JournalEntryLineRequest line) {
		BigDecimal debit = requireAmount(line.getDebitAmount(), "debitAmount is required");
		BigDecimal credit = requireAmount(line.getCreditAmount(), "creditAmount is required");

		if (debit.signum() < 0 || credit.signum() < 0) {
			throw new BadRequestException("debitAmount and creditAmount must be non-negative");
		}
		if (hasMoreThanTwoDecimals(debit) || hasMoreThanTwoDecimals(credit)) {
			throw new BadRequestException("amounts must have at most 2 decimal places");
		}

		boolean hasDebit = debit.signum() > 0;
		boolean hasCredit = credit.signum() > 0;
		if (hasDebit == hasCredit) {
			throw new BadRequestException("each line must have either debitAmount or creditAmount, but not both");
		}

		Account account = accountRepository.findById(line.getAccountId())
			.orElseThrow(() -> new BadRequestException("accountId " + line.getAccountId() + " does not exist"));
		if (!account.isActive()) {
			throw new BadRequestException("accountId " + line.getAccountId() + " must be active");
		}
	}

	private BigDecimal requireAmount(BigDecimal amount, String message) {
		if (amount == null) {
			throw new BadRequestException(message);
		}
		return amount;
	}

	private boolean hasMoreThanTwoDecimals(BigDecimal amount) {
		return amount.stripTrailingZeros().scale() > 2;
	}

	private void validateEntryDate(LocalDate entryDate) {
		LocalDate maxAllowedDate = LocalDate.now().plusDays(1);
		if (entryDate.isAfter(maxAllowedDate)) {
			throw new BadRequestException("entryDate cannot be beyond today + 1 day");
		}
		if (LOCKED_PERIOD_DATES.contains(entryDate)) {
			throw new ConflictException("entryDate falls within a locked accounting period");
		}
	}

	private String normalizeActor(String actor, String message) {
		if (!StringUtils.hasText(actor)) {
			throw new BadRequestException(message);
		}
		return actor.trim();
	}

	private List<JournalEntryLine> buildLines(List<JournalEntryLineRequest> lineRequests) {
		List<JournalEntryLine> lines = new ArrayList<>();
		for (JournalEntryLineRequest lineRequest : lineRequests) {
			Account account = accountRepository.findById(lineRequest.getAccountId())
				.orElseThrow(() -> new BadRequestException("accountId " + lineRequest.getAccountId() + " does not exist"));
			JournalEntryLine line = new JournalEntryLine();
			line.setAccount(account);
			line.setDebitAmount(lineRequest.getDebitAmount().setScale(2, RoundingMode.UNNECESSARY));
			line.setCreditAmount(lineRequest.getCreditAmount().setScale(2, RoundingMode.UNNECESSARY));
			line.setPosted(false);
			lines.add(line);
		}
		return lines;
	}

	private JournalEntry getEntryById(Long id) {
		return journalEntryRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("journal entry not found with id " + id));
	}

	private JournalEntryResponse toResponse(JournalEntry entry) {
		JournalEntryResponse response = new JournalEntryResponse();
		response.setId(entry.getId());
		response.setReferenceNo(entry.getReferenceNo());
		response.setEntryDate(entry.getEntryDate());
		response.setDescription(entry.getDescription());
		response.setStatus(entry.getStatus());
		response.setCreatedBy(entry.getCreatedBy());
		response.setApprovedBy(entry.getApprovedBy());
		response.setRejectedBy(entry.getRejectedBy());
		response.setRejectionReason(entry.getRejectionReason());
		response.setReversalOfId(entry.getReversalOf() == null ? null : entry.getReversalOf().getId());
		response.setLines(entry.getLines().stream().map(this::toLineResponse).toList());
		return response;
	}

	private JournalEntryLineResponse toLineResponse(JournalEntryLine line) {
		JournalEntryLineResponse response = new JournalEntryLineResponse();
		response.setId(line.getId());
		response.setAccountId(line.getAccount().getId());
		response.setAccountCode(line.getAccount().getCode());
		response.setAccountName(line.getAccount().getName());
		response.setDebitAmount(line.getDebitAmount());
		response.setCreditAmount(line.getCreditAmount());
		return response;
	}
}
