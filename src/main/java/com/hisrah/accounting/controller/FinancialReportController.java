package com.hisrah.accounting.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hisrah.accounting.dto.response.ApiResponse;
import com.hisrah.accounting.dto.response.GeneralLedgerResponse;
import com.hisrah.accounting.dto.response.ProfitLossResponse;
import com.hisrah.accounting.dto.response.TrialBalanceResponse;
import com.hisrah.accounting.service.FinancialReportService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Task 3 - Financial Reporting")
@RestController
@RequestMapping("/api/v1/reports")
public class FinancialReportController {

	private final FinancialReportService financialReportService;

	public FinancialReportController(FinancialReportService financialReportService) {
		this.financialReportService = financialReportService;
	}

	@GetMapping("/trial-balance")
	public ResponseEntity<ApiResponse<TrialBalanceResponse>> trialBalance(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
		@RequestParam(defaultValue = "false") boolean includeZeroBalance
	) {
		TrialBalanceResponse response = financialReportService.getTrialBalance(dateFrom, dateTo, includeZeroBalance);
		return ResponseEntity.ok(ApiResponse.success("Trial balance generated", response));
	}

	@GetMapping("/profit-loss")
	public ResponseEntity<ApiResponse<ProfitLossResponse>> profitLoss(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
	) {
		ProfitLossResponse response = financialReportService.getProfitLoss(dateFrom, dateTo);
		return ResponseEntity.ok(ApiResponse.success("Profit & Loss generated", response));
	}

	@GetMapping("/general-ledger")
	public ResponseEntity<ApiResponse<GeneralLedgerResponse>> generalLedger(
		@RequestParam Long accountId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
	) {
		GeneralLedgerResponse response = financialReportService.getGeneralLedger(accountId, dateFrom, dateTo);
		return ResponseEntity.ok(ApiResponse.success("General ledger generated", response));
	}
}
