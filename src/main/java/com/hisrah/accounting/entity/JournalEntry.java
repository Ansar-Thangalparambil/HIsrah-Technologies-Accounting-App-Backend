package com.hisrah.accounting.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "reference_no", nullable = false, unique = true, length = 20)
	private String referenceNo;

	@Column(name = "entry_date", nullable = false)
	private LocalDate entryDate;

	@Column(name = "description", nullable = false, length = 255)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private JournalEntryStatus status = JournalEntryStatus.DRAFT;

	@Column(name = "created_by", nullable = false, length = 100)
	private String createdBy;

	@Column(name = "approved_by", length = 100)
	private String approvedBy;

	@Column(name = "rejected_by", length = 100)
	private String rejectedBy;

	@Column(name = "rejection_reason", length = 500)
	private String rejectionReason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reversal_of")
	private JournalEntry reversalOf;

	@OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<JournalEntryLine> lines = new ArrayList<>();

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public void replaceLines(List<JournalEntryLine> newLines) {
		lines.clear();
		for (JournalEntryLine line : newLines) {
			line.setJournalEntry(this);
			lines.add(line);
		}
	}

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

	public JournalEntry getReversalOf() {
		return reversalOf;
	}

	public void setReversalOf(JournalEntry reversalOf) {
		this.reversalOf = reversalOf;
	}

	public List<JournalEntryLine> getLines() {
		return lines;
	}

	public void setLines(List<JournalEntryLine> lines) {
		this.lines = lines;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
