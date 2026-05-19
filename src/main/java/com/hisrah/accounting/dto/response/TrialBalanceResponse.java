package com.hisrah.accounting.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TrialBalanceResponse {

	private LocalDate dateFrom;
	private LocalDate dateTo;
	private List<TrialBalanceAccountRowResponse> rows;
	private BigDecimal grandTotalDebits;
	private BigDecimal grandTotalCredits;

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

	public List<TrialBalanceAccountRowResponse> getRows() {
		return rows;
	}

	public void setRows(List<TrialBalanceAccountRowResponse> rows) {
		this.rows = rows;
	}

	public BigDecimal getGrandTotalDebits() {
		return grandTotalDebits;
	}

	public void setGrandTotalDebits(BigDecimal grandTotalDebits) {
		this.grandTotalDebits = grandTotalDebits;
	}

	public BigDecimal getGrandTotalCredits() {
		return grandTotalCredits;
	}

	public void setGrandTotalCredits(BigDecimal grandTotalCredits) {
		this.grandTotalCredits = grandTotalCredits;
	}
}
