package com.hisrah.accounting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JournalEntryActionRequest {

	@NotBlank(message = "actionBy is required")
	@Size(max = 100, message = "actionBy must be at most 100 characters")
	private String actionBy;

	@Size(max = 500, message = "reason must be at most 500 characters")
	private String reason;

	public String getActionBy() {
		return actionBy;
	}

	public void setActionBy(String actionBy) {
		this.actionBy = actionBy;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
