package com.hisrah.accounting.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hisrah.accounting.dto.response.ExpenseGroupResponse;
import com.hisrah.accounting.dto.response.GeneralLedgerLineResponse;
import com.hisrah.accounting.dto.response.GeneralLedgerResponse;
import com.hisrah.accounting.dto.response.ProfitLossResponse;
import com.hisrah.accounting.dto.response.TrialBalanceAccountRowResponse;
import com.hisrah.accounting.dto.response.TrialBalanceResponse;
import com.hisrah.accounting.entity.Account;
import com.hisrah.accounting.entity.AccountType;
import com.hisrah.accounting.entity.JournalEntryLine;
import com.hisrah.accounting.exception.BadRequestException;
import com.hisrah.accounting.exception.NotFoundException;
import com.hisrah.accounting.repository.AccountRepository;
import com.hisrah.accounting.repository.JournalEntryLineRepository;

@Service
public class FinancialReportService {

	private final JournalEntryLineRepository journalEntryLineRepository;
	private final AccountRepository accountRepository;

	public FinancialReportService(JournalEntryLineRepository journalEntryLineRepository, AccountRepository accountRepository) {
		this.journalEntryLineRepository = journalEntryLineRepository;
		this.accountRepository = accountRepository;
	}

	@Transactional(readOnly = true)
	public TrialBalanceResponse getTrialBalance(LocalDate dateFrom, LocalDate dateTo, boolean includeZeroBalance) {
		validateDateRange(dateFrom, dateTo);

		List<JournalEntryLine> lines = journalEntryLineRepository.findApprovedLinesBetweenDates(dateFrom, dateTo);
		Map<Long, TrialBalanceAccountRowResponse> rowsByAccount = new HashMap<>();

		for (JournalEntryLine line : lines) {
			Account account = line.getAccount();
			TrialBalanceAccountRowResponse row = rowsByAccount.computeIfAbsent(account.getId(), key -> {
				TrialBalanceAccountRowResponse value = new TrialBalanceAccountRowResponse();
				value.setAccountId(account.getId());
				value.setAccountCode(account.getCode());
				value.setAccountName(account.getName());
				value.setTotalDebits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
				value.setTotalCredits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
				value.setNetBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
				return value;
			});
			row.setTotalDebits(row.getTotalDebits().add(scale(line.getDebitAmount())));
			row.setTotalCredits(row.getTotalCredits().add(scale(line.getCreditAmount())));
			row.setNetBalance(row.getTotalDebits().subtract(row.getTotalCredits()));
		}

		if (includeZeroBalance) {
			for (Account activeAccount : accountRepository.findByActiveTrueOrderByCodeAsc()) {
				rowsByAccount.computeIfAbsent(activeAccount.getId(), key -> {
					TrialBalanceAccountRowResponse value = new TrialBalanceAccountRowResponse();
					value.setAccountId(activeAccount.getId());
					value.setAccountCode(activeAccount.getCode());
					value.setAccountName(activeAccount.getName());
					value.setTotalDebits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
					value.setTotalCredits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
					value.setNetBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
					return value;
				});
			}
		}

		List<TrialBalanceAccountRowResponse> rows = rowsByAccount.values()
			.stream()
			.filter(row -> includeZeroBalance || row.getNetBalance().compareTo(BigDecimal.ZERO) != 0)
			.sorted(Comparator.comparing(TrialBalanceAccountRowResponse::getAccountCode))
			.toList();

		BigDecimal grandDebits = rows.stream().map(TrialBalanceAccountRowResponse::getTotalDebits).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal grandCredits = rows.stream().map(TrialBalanceAccountRowResponse::getTotalCredits).reduce(BigDecimal.ZERO, BigDecimal::add);

		if (grandDebits.compareTo(grandCredits) != 0) {
			throw new IllegalStateException("trial balance integrity issue: grand total debits and credits are not equal");
		}

		TrialBalanceResponse response = new TrialBalanceResponse();
		response.setDateFrom(dateFrom);
		response.setDateTo(dateTo);
		response.setRows(rows);
		response.setGrandTotalDebits(scale(grandDebits));
		response.setGrandTotalCredits(scale(grandCredits));
		return response;
	}

	@Transactional(readOnly = true)
	public ProfitLossResponse getProfitLoss(LocalDate dateFrom, LocalDate dateTo) {
		validateDateRange(dateFrom, dateTo);

		List<JournalEntryLine> revenueLines = journalEntryLineRepository.findApprovedLinesByAccountTypeBetweenDates(AccountType.REVENUE, dateFrom, dateTo);
		List<JournalEntryLine> expenseLines = journalEntryLineRepository.findApprovedLinesByAccountTypeBetweenDates(AccountType.EXPENSE, dateFrom, dateTo);

		BigDecimal revenue = revenueLines.stream()
			.map(line -> scale(line.getCreditAmount()).subtract(scale(line.getDebitAmount())))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalExpenses = expenseLines.stream()
			.map(line -> scale(line.getDebitAmount()).subtract(scale(line.getCreditAmount())))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal cogs = expenseLines.stream()
			.filter(line -> isCodeInRange(line.getAccount().getCode(), 5000, 5999))
			.map(line -> scale(line.getDebitAmount()).subtract(scale(line.getCreditAmount())))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		Map<Long, ExpenseGroupResponse> grouped = new HashMap<>();
		for (JournalEntryLine line : expenseLines) {
			Account account = line.getAccount();
			Account parent = account.getParent() == null ? account : account.getParent();

			ExpenseGroupResponse group = grouped.computeIfAbsent(parent.getId(), key -> {
				ExpenseGroupResponse value = new ExpenseGroupResponse();
				value.setParentAccountId(parent.getId());
				value.setParentAccountCode(parent.getCode());
				value.setParentAccountName(parent.getName());
				value.setSubtotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
				value.setLines(new ArrayList<>());
				return value;
			});

			TrialBalanceAccountRowResponse lineRow = new TrialBalanceAccountRowResponse();
			lineRow.setAccountId(account.getId());
			lineRow.setAccountCode(account.getCode());
			lineRow.setAccountName(account.getName());
			lineRow.setTotalDebits(scale(line.getDebitAmount()));
			lineRow.setTotalCredits(scale(line.getCreditAmount()));
			lineRow.setNetBalance(scale(line.getDebitAmount()).subtract(scale(line.getCreditAmount())));
			group.getLines().add(lineRow);
			group.setSubtotal(group.getSubtotal().add(lineRow.getNetBalance()));
		}

		List<ExpenseGroupResponse> groups = grouped.values().stream()
			.peek(group -> group.getLines().sort(Comparator.comparing(TrialBalanceAccountRowResponse::getAccountCode)))
			.sorted(Comparator.comparing(ExpenseGroupResponse::getParentAccountCode))
			.toList();

		ProfitLossResponse response = new ProfitLossResponse();
		response.setDateFrom(dateFrom);
		response.setDateTo(dateTo);
		response.setRevenue(scale(revenue));
		response.setCogs(scale(cogs));
		response.setGrossProfit(scale(revenue.subtract(cogs)));
		response.setTotalExpenses(scale(totalExpenses));
		response.setNetProfit(scale(revenue.subtract(totalExpenses)));
		response.setExpenseGroups(groups);
		return response;
	}

	@Transactional(readOnly = true)
	public GeneralLedgerResponse getGeneralLedger(Long accountId, LocalDate dateFrom, LocalDate dateTo) {
		validateDateRange(dateFrom, dateTo);
		Account account = accountRepository.findFirstByIdAndActiveTrue(accountId)
			.orElseThrow(() -> new NotFoundException("active account not found with id " + accountId));

		List<JournalEntryLine> openingLines = journalEntryLineRepository.findApprovedLinesForAccountBeforeDate(accountId, dateFrom);
		BigDecimal openingBalance = openingLines.stream()
			.map(line -> scale(line.getDebitAmount()).subtract(scale(line.getCreditAmount())))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<JournalEntryLine> periodLines = journalEntryLineRepository.findApprovedLinesForAccountBetweenDates(accountId, dateFrom, dateTo);
		List<GeneralLedgerLineResponse> lineResponses = new ArrayList<>();
		BigDecimal running = openingBalance;
		for (JournalEntryLine line : periodLines) {
			BigDecimal movement = scale(line.getDebitAmount()).subtract(scale(line.getCreditAmount()));
			running = running.add(movement);

			GeneralLedgerLineResponse lineResponse = new GeneralLedgerLineResponse();
			lineResponse.setLineId(line.getId());
			lineResponse.setEntryDate(line.getJournalEntry().getEntryDate());
			lineResponse.setReferenceNo(line.getJournalEntry().getReferenceNo());
			lineResponse.setDescription(line.getJournalEntry().getDescription());
			lineResponse.setDebitAmount(scale(line.getDebitAmount()));
			lineResponse.setCreditAmount(scale(line.getCreditAmount()));
			lineResponse.setMovement(scale(movement));
			lineResponse.setRunningBalance(scale(running));
			lineResponses.add(lineResponse);
		}

		GeneralLedgerResponse response = new GeneralLedgerResponse();
		response.setAccountId(account.getId());
		response.setAccountCode(account.getCode());
		response.setAccountName(account.getName());
		response.setDateFrom(dateFrom);
		response.setDateTo(dateTo);
		response.setOpeningBalance(scale(openingBalance));
		response.setClosingBalance(scale(running));
		response.setLines(lineResponses);
		return response;
	}

	private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom == null || dateTo == null) {
			throw new BadRequestException("dateFrom and dateTo are required");
		}
		if (dateFrom.isAfter(dateTo)) {
			throw new BadRequestException("dateFrom cannot be after dateTo");
		}
	}

	private boolean isCodeInRange(String code, int from, int to) {
		try {
			int value = Integer.parseInt(code);
			return value >= from && value <= to;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	private BigDecimal scale(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}
}
