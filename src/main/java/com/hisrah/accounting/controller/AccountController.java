package com.hisrah.accounting.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hisrah.accounting.dto.request.AccountUpsertRequest;
import com.hisrah.accounting.dto.response.AccountResponse;
import com.hisrah.accounting.dto.response.AccountTreeResponse;
import com.hisrah.accounting.dto.response.ApiResponse;
import com.hisrah.accounting.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<AccountResponse>> create(@Valid @RequestBody AccountUpsertRequest request) {
		AccountResponse created = accountService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Account created", created));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<AccountResponse>>> list() {
		return ResponseEntity.ok(ApiResponse.success("Accounts fetched", accountService.list()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AccountResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Account fetched", accountService.getById(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<AccountResponse>> update(
		@PathVariable Long id,
		@Valid @RequestBody AccountUpsertRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success("Account updated", accountService.update(id, request)));
	}

	@PatchMapping("/{id}/deactivate")
	public ResponseEntity<ApiResponse<AccountResponse>> deactivate(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Account deactivated", accountService.deactivate(id)));
	}

	@PatchMapping("/{id}/activate")
	public ResponseEntity<ApiResponse<AccountResponse>> activate(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Account activated", accountService.activate(id)));
	}

	@GetMapping("/tree")
	public ResponseEntity<ApiResponse<List<AccountTreeResponse>>> tree() {
		return ResponseEntity.ok(ApiResponse.success("Account tree fetched", accountService.tree()));
	}
}
