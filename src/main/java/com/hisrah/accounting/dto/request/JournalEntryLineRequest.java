package com.hisrah.accounting.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public class JournalEntryLineRequest {

	@NotNull(message = "accountId is required")
	private Long accountId;

	@NotNull(message = "debitAmount is required")
	private BigDecimal debitAmount;

	@NotNull(message = "creditAmount is required")
	private BigDecimal creditAmount;

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public BigDecimal getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(BigDecimal debitAmount) {
		this.debitAmount = debitAmount;
	}

	public BigDecimal getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(BigDecimal creditAmount) {
		this.creditAmount = creditAmount;
	}
}
