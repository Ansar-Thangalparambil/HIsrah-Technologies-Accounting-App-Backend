package com.hisrah.accounting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hisrah.accounting.entity.JournalEntry;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

	long countByReferenceNoStartingWith(String prefix);

	List<JournalEntry> findAllByOrderByIdDesc();
}
