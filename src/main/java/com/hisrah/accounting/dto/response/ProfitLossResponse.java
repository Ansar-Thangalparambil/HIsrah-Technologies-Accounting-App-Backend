package com.hisrah.accounting.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProfitLossResponse {

	private LocalDate dateFrom;
	private LocalDate dateTo;
	private BigDecimal revenue;
	private BigDecimal cogs;
	private BigDecimal grossProfit;
	private BigDecimal totalExpenses;
	private BigDecimal netProfit;
	private List<ExpenseGroupResponse> expenseGroups;

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

	public BigDecimal getRevenue() {
		return revenue;
	}

	public void setRevenue(BigDecimal revenue) {
		this.revenue = revenue;
	}

	public BigDecimal getCogs() {
		return cogs;
	}

	public void setCogs(BigDecimal cogs) {
		this.cogs = cogs;
	}

	public BigDecimal getGrossProfit() {
		return grossProfit;
	}

	public void setGrossProfit(BigDecimal grossProfit) {
		this.grossProfit = grossProfit;
	}

	public BigDecimal getTotalExpenses() {
		return totalExpenses;
	}

	public void setTotalExpenses(BigDecimal totalExpenses) {
		this.totalExpenses = totalExpenses;
	}

	public BigDecimal getNetProfit() {
		return netProfit;
	}

	public void setNetProfit(BigDecimal netProfit) {
		this.netProfit = netProfit;
	}

	public List<ExpenseGroupResponse> getExpenseGroups() {
		return expenseGroups;
	}

	public void setExpenseGroups(List<ExpenseGroupResponse> expenseGroups) {
		this.expenseGroups = expenseGroups;
	}
}
