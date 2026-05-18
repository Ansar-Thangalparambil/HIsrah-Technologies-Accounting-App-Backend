package com.hisrah.accounting.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.hisrah.accounting.entity.JournalEntryStatus;

public class JournalEntryResponse {

	private Long id;
	private String referenceNo;
	private LocalDate entryDate;
	private String description;
	private JournalEntryStatus status;
	private String createdBy;
	private String approvedBy;
	private String rejectedBy;
	private String rejectionReason;
	private Long reversalOfId;
	private List<JournalEntryLineResponse> lines;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

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

	public JournalEntryStatus getStatus() {
		return status;
	}

	public void setStatus(JournalEntryStatus status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public String getRejectedBy() {
		return rejectedBy;
	}

	public void setRejectedBy(String rejectedBy) {
		this.rejectedBy = rejectedBy;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public Long getReversalOfId() {
		return reversalOfId;
	}

	public void setReversalOfId(Long reversalOfId) {
		this.reversalOfId = reversalOfId;
	}

	public List<JournalEntryLineResponse> getLines() {
		return lines;
	}

	public void setLines(List<JournalEntryLineResponse> lines) {
		this.lines = lines;
	}
}
