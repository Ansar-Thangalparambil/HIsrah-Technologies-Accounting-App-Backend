package com.hisrah.accounting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hisrah.accounting.dto.request.JournalEntryActionRequest;
import com.hisrah.accounting.dto.request.JournalEntryLineRequest;
import com.hisrah.accounting.dto.request.JournalEntryUpsertRequest;
import com.hisrah.accounting.dto.response.JournalEntryResponse;
import com.hisrah.accounting.entity.Account;
import com.hisrah.accounting.entity.AccountType;
import com.hisrah.accounting.entity.JournalEntry;
import com.hisrah.accounting.entity.JournalEntryStatus;
import com.hisrah.accounting.exception.ForbiddenException;
import com.hisrah.accounting.exception.UnprocessableEntityException;
import com.hisrah.accounting.repository.AccountRepository;
import com.hisrah.accounting.repository.JournalEntryLineRepository;
import com.hisrah.accounting.repository.JournalEntryRepository;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {

	@Mock
	private JournalEntryRepository journalEntryRepository;

	@Mock
	private JournalEntryLineRepository journalEntryLineRepository;

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private JournalEntryService journalEntryService;

	@Test
	void create_ShouldFail_WhenEntryIsUnbalanced() {
		JournalEntryUpsertRequest request = buildRequest(
			List.of(
				buildLine(1L, "100.00", "0.00"),
				buildLine(2L, "0.00", "90.00")
			)
		);

		Account account1 = buildAccount(1L, "1000");
		Account account2 = buildAccount(2L, "5000");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(account1));
		when(accountRepository.findById(2L)).thenReturn(Optional.of(account2));

		assertThrows(UnprocessableEntityException.class, () -> journalEntryService.create(request));
	}

	@Test
	void create_ShouldGenerateReferenceAndPersistDraft_WhenValid() {
		JournalEntryUpsertRequest request = buildRequest(
			List.of(
				buildLine(1L, "100.00", "0.00"),
				buildLine(2L, "0.00", "100.00")
			)
		);
		Account account1 = buildAccount(1L, "1000");
		Account account2 = buildAccount(2L, "5000");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(account1));
		when(accountRepository.findById(2L)).thenReturn(Optional.of(account2));
		when(journalEntryRepository.countByReferenceNoStartingWith(any())).thenReturn(0L);
		when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> {
			JournalEntry saved = invocation.getArgument(0);
			saved.setId(10L);
			return saved;
		});

		JournalEntryResponse response = journalEntryService.create(request);

		assertEquals(10L, response.getId());
		assertEquals(JournalEntryStatus.DRAFT, response.getStatus());
		assertEquals("JE-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0001", response.getReferenceNo());
	}

	@Test
	void approve_ShouldReject_WhenApproverIsCreator() {
		JournalEntry entry = new JournalEntry();
		entry.setId(7L);
		entry.setStatus(JournalEntryStatus.PENDING_APPROVAL);
		entry.setCreatedBy("ansar");

		when(journalEntryRepository.findById(7L)).thenReturn(Optional.of(entry));

		JournalEntryActionRequest actionRequest = new JournalEntryActionRequest();
		actionRequest.setActionBy("ansar");

		assertThrows(ForbiddenException.class, () -> journalEntryService.approve(7L, actionRequest));
	}

	@Test
	void reverse_ShouldCreateApprovedMirrorEntry_WhenOriginalApproved() {
		Account account = buildAccount(1L, "1000");
		JournalEntry original = new JournalEntry();
		original.setId(9L);
		original.setReferenceNo("JE-20260518-0001");
		original.setStatus(JournalEntryStatus.APPROVED);
		original.setCreatedBy("maker");

		com.hisrah.accounting.entity.JournalEntryLine line = new com.hisrah.accounting.entity.JournalEntryLine();
		line.setAccount(account);
		line.setDebitAmount(new BigDecimal("100.00"));
		line.setCreditAmount(new BigDecimal("0.00"));
		line.setJournalEntry(original);
		original.setLines(List.of(line));

		when(journalEntryRepository.findById(9L)).thenReturn(Optional.of(original));
		when(journalEntryRepository.countByReferenceNoStartingWith(any())).thenReturn(0L);
		when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

		JournalEntryActionRequest actionRequest = new JournalEntryActionRequest();
		actionRequest.setActionBy("manager1");

		JournalEntryResponse reversal = journalEntryService.reverse(9L, actionRequest);

		assertEquals(JournalEntryStatus.APPROVED, reversal.getStatus());
		assertEquals(9L, reversal.getReversalOfId());
		assertEquals(new BigDecimal("0.00"), reversal.getLines().get(0).getDebitAmount());
		assertEquals(new BigDecimal("100.00"), reversal.getLines().get(0).getCreditAmount());
	}

	private JournalEntryUpsertRequest buildRequest(List<JournalEntryLineRequest> lines) {
		JournalEntryUpsertRequest request = new JournalEntryUpsertRequest();
		request.setEntryDate(LocalDate.now());
		request.setDescription("Manual Entry");
		request.setCreatedBy("ansar");
		request.setLines(lines);
		return request;
	}

	private JournalEntryLineRequest buildLine(Long accountId, String debit, String credit) {
		JournalEntryLineRequest line = new JournalEntryLineRequest();
		line.setAccountId(accountId);
		line.setDebitAmount(new BigDecimal(debit));
		line.setCreditAmount(new BigDecimal(credit));
		return line;
	}

	private Account buildAccount(Long id, String code) {
		Account account = new Account();
		account.setId(id);
		account.setCode(code);
		account.setName("Account " + code);
		account.setType(AccountType.ASSET);
		account.setActive(true);
		return account;
	}
}
