package com.hisrah.accounting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hisrah.accounting.entity.JournalEntryLine;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

	boolean existsByAccountIdAndPostedTrue(Long accountId);

	List<JournalEntryLine> findByJournalEntryId(Long journalEntryId);
}
