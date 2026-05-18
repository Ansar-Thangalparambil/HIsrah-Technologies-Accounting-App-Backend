package com.hisrah.accounting.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class JournalEntryUpsertRequest {

	@NotNull(message = "entryDate is required")
	private LocalDate entryDate;

	@NotBlank(message = "description is required")
	@Size(max = 255, message = "description must be at most 255 characters")
	private String description;

	@NotBlank(message = "createdBy is required")
	@Size(max = 100, message = "createdBy must be at most 100 characters")
	private String createdBy;

	@Valid
	@NotEmpty(message = "lines are required")
	private List<JournalEntryLineRequest> lines;

	public LocalDate getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public List<JournalEntryLineRequest> getLines() {
		return lines;
	}

	public void setLines(List<JournalEntryLineRequest> lines) {
		this.lines = lines;
	}
}
