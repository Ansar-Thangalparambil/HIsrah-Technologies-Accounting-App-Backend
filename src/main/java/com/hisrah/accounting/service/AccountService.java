package com.hisrah.accounting.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hisrah.accounting.dto.request.AccountUpsertRequest;
import com.hisrah.accounting.dto.response.AccountResponse;
import com.hisrah.accounting.dto.response.AccountTreeResponse;
import com.hisrah.accounting.entity.Account;
import com.hisrah.accounting.entity.AccountType;
import com.hisrah.accounting.exception.BadRequestException;
import com.hisrah.accounting.exception.ConflictException;
import com.hisrah.accounting.exception.NotFoundException;
import com.hisrah.accounting.repository.AccountRepository;
import com.hisrah.accounting.repository.JournalEntryLineRepository;

@Service
public class AccountService {

	private static final int MAX_DEPTH = 4;
	private final AccountRepository accountRepository;
	private final JournalEntryLineRepository journalEntryLineRepository;

	public AccountService(AccountRepository accountRepository, JournalEntryLineRepository journalEntryLineRepository) {
		this.accountRepository = accountRepository;
		this.journalEntryLineRepository = journalEntryLineRepository;
	}

	@Transactional
	public AccountResponse create(AccountUpsertRequest request) {
		if (accountRepository.existsByCode(request.getCode())) {
			throw new ConflictException("account code must be unique");
		}

		validateCodeRange(request.getType(), request.getCode());
		Account parent = resolveAndValidateParent(request.getParentId(), request.getType(), null);
		enforceDepth(parent, null);

		Account account = new Account();
		account.setCode(request.getCode());
		account.setName(request.getName().trim());
		account.setType(request.getType());
		account.setParent(parent);
		account.setActive(true);

		return toResponse(accountRepository.save(account));
	}

	@Transactional
	public AccountResponse update(Long id, AccountUpsertRequest request) {
		Account existing = getEntityById(id);

		if (accountRepository.existsByCodeAndIdNot(request.getCode(), id)) {
			throw new ConflictException("account code must be unique");
		}

		validateCodeRange(request.getType(), request.getCode());
		Account parent = resolveAndValidateParent(request.getParentId(), request.getType(), existing);
		enforceDepth(parent, existing.getId());

		existing.setCode(request.getCode());
		existing.setName(request.getName().trim());
		existing.setType(request.getType());
		existing.setParent(parent);

		return toResponse(accountRepository.save(existing));
	}

	@Transactional(readOnly = true)
	public List<AccountResponse> list() {
		return accountRepository.findAllByOrderByCodeAsc().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public AccountResponse getById(Long id) {
		return toResponse(getEntityById(id));
	}

	@Transactional(readOnly = true)
	public List<AccountTreeResponse> tree() {
		List<Account> accounts = accountRepository.findAllByOrderByCodeAsc();
		Map<Long, AccountTreeResponse> nodes = new HashMap<>();
		List<AccountTreeResponse> roots = new ArrayList<>();

		for (Account account : accounts) {
			nodes.put(account.getId(), toTreeResponse(account));
		}

		for (Account account : accounts) {
			AccountTreeResponse node = nodes.get(account.getId());
			if (account.getParent() == null) {
				roots.add(node);
			} else {
				AccountTreeResponse parent = nodes.get(account.getParent().getId());
				if (parent != null) {
					parent.getChildren().add(node);
				}
			}
		}

		roots.sort(Comparator.comparing(AccountTreeResponse::getCode));
		return roots;
	}

	@Transactional
	public AccountResponse deactivate(Long id) {
		Account account = getEntityById(id);
		if (!account.isActive()) {
			return toResponse(account);
		}
		if (journalEntryLineRepository.existsByAccountIdAndPostedTrue(account.getId())) {
			throw new ConflictException("account cannot be deactivated because it has posted journal entry lines");
		}
		account.setActive(false);
		return toResponse(accountRepository.save(account));
	}

	@Transactional
	public AccountResponse activate(Long id) {
		Account account = getEntityById(id);
		account.setActive(true);
		return toResponse(accountRepository.save(account));
	}

	private Account getEntityById(Long id) {
		return accountRepository.findById(id).orElseThrow(() -> new NotFoundException("account not found with id " + id));
	}

	private Account resolveAndValidateParent(Long parentId, AccountType requestType, Account currentAccount) {
		if (parentId == null) {
			return null;
		}
		Account parent = accountRepository.findById(parentId)
			.orElseThrow(() -> new BadRequestException("parentId must reference an existing account"));

		if (!parent.isActive()) {
			throw new BadRequestException("parentId must reference an active account");
		}

		if (currentAccount != null) {
			if (parent.getId().equals(currentAccount.getId())) {
				throw new BadRequestException("account cannot be parent of itself");
			}
			if (isDescendant(parent, currentAccount.getId())) {
				throw new BadRequestException("account hierarchy cannot contain cycles");
			}
		}

		if (parent.getType() != requestType) {
			throw new BadRequestException("parent and child must have the same account type");
		}

		return parent;
	}

	private void enforceDepth(Account parent, Long movingNodeId) {
		int targetNodeDepth = calculateDepth(parent);
		if (movingNodeId == null) {
			if (targetNodeDepth > MAX_DEPTH) {
				throw new BadRequestException("account hierarchy depth cannot exceed 4 levels");
			}
			return;
		}

		int subtreeHeight = calculateSubtreeHeight(movingNodeId);
		if (targetNodeDepth + subtreeHeight - 1 > MAX_DEPTH) {
			throw new BadRequestException("account hierarchy depth cannot exceed 4 levels");
		}
	}

	private int calculateDepth(Account parent) {
		int depth = 1;
		Account cursor = parent;
		while (cursor != null) {
			depth++;
			cursor = cursor.getParent();
		}
		return depth;
	}

	private int calculateSubtreeHeight(Long rootId) {
		List<Account> accounts = accountRepository.findAllByOrderByCodeAsc();
		Map<Long, List<Long>> childrenByParent = new HashMap<>();
		for (Account account : accounts) {
			Long parentId = account.getParent() == null ? null : account.getParent().getId();
			if (parentId != null) {
				childrenByParent.computeIfAbsent(parentId, key -> new ArrayList<>()).add(account.getId());
			}
		}
		return findMaxDepthFromRoot(rootId, childrenByParent, 1);
	}

	private int findMaxDepthFromRoot(Long nodeId, Map<Long, List<Long>> childrenByParent, int currentDepth) {
		List<Long> children = childrenByParent.getOrDefault(nodeId, List.of());
		int maxDepth = currentDepth;
		for (Long childId : children) {
			if (!Objects.equals(childId, nodeId)) {
				maxDepth = Math.max(maxDepth, findMaxDepthFromRoot(childId, childrenByParent, currentDepth + 1));
			}
		}
		return maxDepth;
	}

	private boolean isDescendant(Account candidateParent, Long currentAccountId) {
		Account cursor = candidateParent;
		while (cursor != null) {
			if (cursor.getId().equals(currentAccountId)) {
				return true;
			}
			cursor = cursor.getParent();
		}
		return false;
	}

	private void validateCodeRange(AccountType type, String code) {
		char leadingDigit = code.charAt(0);
		boolean valid = switch (type) {
			case ASSET -> leadingDigit == '1';
			case LIABILITY -> leadingDigit == '2';
			case EQUITY -> leadingDigit == '3';
			case REVENUE -> leadingDigit == '4';
			case EXPENSE -> leadingDigit == '5' || leadingDigit == '6';
		};
		if (!valid) {
			throw new BadRequestException("account code range does not match account type (ASSET 1xxx, LIABILITY 2xxx, EQUITY 3xxx, REVENUE 4xxx, EXPENSE 5xxx/6xxx)");
		}
	}

	private AccountResponse toResponse(Account account) {
		AccountResponse response = new AccountResponse();
		response.setId(account.getId());
		response.setCode(account.getCode());
		response.setName(account.getName());
		response.setType(account.getType());
		response.setParentId(account.getParent() == null ? null : account.getParent().getId());
		response.setActive(account.isActive());
		return response;
	}

	private AccountTreeResponse toTreeResponse(Account account) {
		AccountTreeResponse response = new AccountTreeResponse();
		response.setId(account.getId());
		response.setCode(account.getCode());
		response.setName(account.getName());
		response.setType(account.getType());
		response.setActive(account.isActive());
		return response;
	}
}
