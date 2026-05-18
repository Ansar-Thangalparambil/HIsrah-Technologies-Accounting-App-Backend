package com.hisrah.accounting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hisrah.accounting.dto.request.AccountUpsertRequest;
import com.hisrah.accounting.dto.response.AccountResponse;
import com.hisrah.accounting.entity.Account;
import com.hisrah.accounting.entity.AccountType;
import com.hisrah.accounting.exception.BadRequestException;
import com.hisrah.accounting.exception.ConflictException;
import com.hisrah.accounting.repository.AccountRepository;
import com.hisrah.accounting.repository.JournalEntryLineRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private JournalEntryLineRepository journalEntryLineRepository;

	@InjectMocks
	private AccountService accountService;

	@Test
	void create_ShouldPersistAccount_WhenRequestIsValid() {
		AccountUpsertRequest request = new AccountUpsertRequest();
		request.setCode("1110");
		request.setName("Bank Account");
		request.setType(AccountType.ASSET);

		when(accountRepository.existsByCode("1110")).thenReturn(false);
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
			Account saved = invocation.getArgument(0);
			saved.setId(1L);
			return saved;
		});

		AccountResponse response = accountService.create(request);

		assertEquals(1L, response.getId());
		assertEquals("1110", response.getCode());
		assertEquals("Bank Account", response.getName());
		assertEquals(AccountType.ASSET, response.getType());
		assertTrue(response.isActive());
	}

	@Test
	void create_ShouldThrowConflict_WhenCodeAlreadyExists() {
		AccountUpsertRequest request = new AccountUpsertRequest();
		request.setCode("1110");
		request.setName("Duplicate");
		request.setType(AccountType.ASSET);

		when(accountRepository.existsByCode("1110")).thenReturn(true);

		assertThrows(ConflictException.class, () -> accountService.create(request));
	}

	@Test
	void create_ShouldThrowBadRequest_WhenCodeRangeDoesNotMatchType() {
		AccountUpsertRequest request = new AccountUpsertRequest();
		request.setCode("1110");
		request.setName("Invalid Type");
		request.setType(AccountType.LIABILITY);

		when(accountRepository.existsByCode("1110")).thenReturn(false);

		assertThrows(BadRequestException.class, () -> accountService.create(request));
	}

	@Test
	void deactivate_ShouldThrowConflict_WhenPostedJournalLineExists() {
		Account account = new Account();
		account.setId(10L);
		account.setCode("1110");
		account.setName("Bank");
		account.setType(AccountType.ASSET);
		account.setActive(true);

		when(accountRepository.findById(10L)).thenReturn(Optional.of(account));
		when(journalEntryLineRepository.existsByAccountIdAndPostedTrue(10L)).thenReturn(true);

		assertThrows(ConflictException.class, () -> accountService.deactivate(10L));
	}

	@Test
	void deactivate_ShouldSetActiveFalse_WhenNoPostedJournalLine() {
		Account account = new Account();
		account.setId(11L);
		account.setCode("1111");
		account.setName("Cash");
		account.setType(AccountType.ASSET);
		account.setActive(true);

		when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
		when(journalEntryLineRepository.existsByAccountIdAndPostedTrue(11L)).thenReturn(false);
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AccountResponse response = accountService.deactivate(11L);

		assertFalse(response.isActive());
		verify(accountRepository).save(account);
	}

	@Test
	void update_ShouldRejectMove_WhenSubtreeWouldExceedMaxDepth() {
		Account root = new Account();
		root.setId(1L);
		root.setType(AccountType.ASSET);
		root.setActive(true);

		Account current = new Account();
		current.setId(2L);
		current.setCode("1200");
		current.setName("Current Assets");
		current.setType(AccountType.ASSET);
		current.setParent(root);
		current.setActive(true);

		Account grandChild = new Account();
		grandChild.setId(3L);
		grandChild.setCode("1210");
		grandChild.setName("Bank");
		grandChild.setType(AccountType.ASSET);
		grandChild.setParent(current);
		grandChild.setActive(true);

		Account level2 = new Account();
		level2.setId(5L);
		level2.setType(AccountType.ASSET);
		level2.setActive(true);
		level2.setParent(root);

		Account deepParent = new Account();
		deepParent.setId(4L);
		deepParent.setType(AccountType.ASSET);
		deepParent.setActive(true);
		deepParent.setParent(level2);

		AccountUpsertRequest request = new AccountUpsertRequest();
		request.setCode("1200");
		request.setName("Current Assets");
		request.setType(AccountType.ASSET);
		request.setParentId(4L);

		when(accountRepository.findById(2L)).thenReturn(Optional.of(current));
		when(accountRepository.existsByCodeAndIdNot("1200", 2L)).thenReturn(false);
		when(accountRepository.findById(4L)).thenReturn(Optional.of(deepParent));
		when(accountRepository.findAllByOrderByCodeAsc()).thenReturn(List.of(root, current, grandChild, level2, deepParent));

		assertThrows(BadRequestException.class, () -> accountService.update(2L, request));
	}
}
