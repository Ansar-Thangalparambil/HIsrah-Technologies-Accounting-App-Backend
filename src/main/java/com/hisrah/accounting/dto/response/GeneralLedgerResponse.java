package com.hisrah.accounting.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GeneralLedgerResponse {

	private Long accountId;
	private String accountCode;
	private String accountName;
	private LocalDate dateFrom;
	private LocalDate dateTo;
	private BigDecimal openingBalance;
	private BigDecimal closingBalance;
	private List<GeneralLedgerLineResponse> lines;

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public LocalDate getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDate dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDate getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDate dateTo) {
		this.dateTo = dateTo;
	}

	public BigDecimal getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(BigDecimal openingBalance) {
		this.openingBalance = openingBalance;
	}

	public BigDecimal getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(BigDecimal closingBalance) {
		this.closingBalance = closingBalance;
	}

	public List<GeneralLedgerLineResponse> getLines() {
		return lines;
	}

	public void setLines(List<GeneralLedgerLineResponse> lines) {
		this.lines = lines;
	}
}
