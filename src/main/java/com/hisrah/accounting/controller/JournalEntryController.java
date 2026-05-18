package com.hisrah.accounting.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hisrah.accounting.dto.request.JournalEntryActionRequest;
import com.hisrah.accounting.dto.request.JournalEntryUpsertRequest;
import com.hisrah.accounting.dto.response.ApiResponse;
import com.hisrah.accounting.dto.response.JournalEntryResponse;
import com.hisrah.accounting.service.JournalEntryService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Task 2 - Manual Journal Entry")
@RestController
@RequestMapping("/api/journal-entries")
public class JournalEntryController {

	private final JournalEntryService journalEntryService;

	public JournalEntryController(JournalEntryService journalEntryService) {
		this.journalEntryService = journalEntryService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<JournalEntryResponse>> create(@Valid @RequestBody JournalEntryUpsertRequest request) {
		JournalEntryResponse created = journalEntryService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Journal entry created", created));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<JournalEntryResponse>>> list() {
		return ResponseEntity.ok(ApiResponse.success("Journal entries fetched", journalEntryService.list()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<JournalEntryResponse>> getById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Journal entry fetched", journalEntryService.getById(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<JournalEntryResponse>> update(
		@PathVariable Long id,
		@Valid @RequestBody JournalEntryUpsertRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success("Journal entry updated", journalEntryService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		journalEntryService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Journal entry deleted", null));
	}

	@PatchMapping("/{id}/submit")
	public ResponseEntity<ApiResponse<JournalEntryResponse>> submit(
		@PathVariable Long id,
		@Valid @RequestBody JournalEntryActionRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success("Journal entry submitted", journalEntryService.submit(id, request)));
	}

	@PatchMapping("/{id}/approve")
	public ResponseEntity<ApiResponse<JournalEntryResponse>> approve(
		@PathVariable Long id,
		@Valid @RequestBody JournalEntryActionRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success("Journal entry approved", journalEntryService.approve(id, request)));
	}

	@PatchMapping("/{id}/reject")
	public ResponseEntity<ApiResponse<JournalEntryResponse>> reject(
		@PathVariable Long id,
		@Valid @RequestBody JournalEntryActionRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success("Journal entry rejected", journalEntryService.reject(id, request)));
	}

	@PatchMapping("/{id}/reverse")
	public ResponseEntity<ApiResponse<JournalEntryResponse>> reverse(
		@PathVariable Long id,
		@Valid @RequestBody JournalEntryActionRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success("Journal entry reversed", journalEntryService.reverse(id, request)));
	}
}
