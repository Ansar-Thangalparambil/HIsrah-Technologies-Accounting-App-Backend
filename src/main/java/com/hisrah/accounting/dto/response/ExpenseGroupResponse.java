package com.hisrah.accounting.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class ExpenseGroupResponse {

	private Long parentAccountId;
	private String parentAccountCode;
	private String parentAccountName;
	private BigDecimal subtotal;
	private List<TrialBalanceAccountRowResponse> lines;

	public Long getParentAccountId() {
		return parentAccountId;
	}

	public void setParentAccountId(Long parentAccountId) {
		this.parentAccountId = parentAccountId;
	}

	public String getParentAccountCode() {
		return parentAccountCode;
	}

	public void setParentAccountCode(String parentAccountCode) {
		this.parentAccountCode = parentAccountCode;
	}

	public String getParentAccountName() {
		return parentAccountName;
	}

	public void setParentAccountName(String parentAccountName) {
		this.parentAccountName = parentAccountName;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public List<TrialBalanceAccountRowResponse> getLines() {
		return lines;
	}

	public void setLines(List<TrialBalanceAccountRowResponse> lines) {
		this.lines = lines;
	}
}
