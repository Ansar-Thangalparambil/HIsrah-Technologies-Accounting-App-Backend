package com.hisrah.accounting.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GeneralLedgerLineResponse {

	private Long lineId;
	private LocalDate entryDate;
	private String referenceNo;
	private String description;
	private BigDecimal debitAmount;
	private BigDecimal creditAmount;
	private BigDecimal movement;
	private BigDecimal runningBalance;

	public Long getLineId() {
		return lineId;
	}

	public void setLineId(Long lineId) {
		this.lineId = lineId;
	}

	public LocalDate getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public BigDecimal getMovement() {
		return movement;
	}

	public void setMovement(BigDecimal movement) {
		this.movement = movement;
	}

	public BigDecimal getRunningBalance() {
		return runningBalance;
	}

	public void setRunningBalance(BigDecimal runningBalance) {
		this.runningBalance = runningBalance;
	}
}
