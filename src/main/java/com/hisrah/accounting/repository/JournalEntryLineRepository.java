package com.hisrah.accounting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hisrah.accounting.entity.AccountType;
import com.hisrah.accounting.entity.JournalEntryLine;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

	boolean existsByAccountIdAndPostedTrue(Long accountId);

	List<JournalEntryLine> findByJournalEntryId(Long journalEntryId);

	@Query("""
		select l from JournalEntryLine l
		join l.journalEntry je
		join l.account a
		where je.status = com.hisrah.accounting.entity.JournalEntryStatus.APPROVED
		  and je.entryDate >= :dateFrom
		  and je.entryDate <= :dateTo
		order by a.code asc, l.id asc
	""")
	List<JournalEntryLine> findApprovedLinesBetweenDates(java.time.LocalDate dateFrom, java.time.LocalDate dateTo);

	@Query("""
		select l from JournalEntryLine l
		join l.journalEntry je
		where je.status = com.hisrah.accounting.entity.JournalEntryStatus.APPROVED
		  and l.account.id = :accountId
		  and je.entryDate < :dateFrom
	""")
	List<JournalEntryLine> findApprovedLinesForAccountBeforeDate(Long accountId, java.time.LocalDate dateFrom);

	@Query("""
		select l from JournalEntryLine l
		join l.journalEntry je
		where je.status = com.hisrah.accounting.entity.JournalEntryStatus.APPROVED
		  and l.account.id = :accountId
		  and je.entryDate >= :dateFrom
		  and je.entryDate <= :dateTo
		order by je.entryDate asc, l.id asc
	""")
	List<JournalEntryLine> findApprovedLinesForAccountBetweenDates(Long accountId, java.time.LocalDate dateFrom, java.time.LocalDate dateTo);

	@Query("""
		select l from JournalEntryLine l
		join l.journalEntry je
		where je.status = com.hisrah.accounting.entity.JournalEntryStatus.APPROVED
		  and je.entryDate >= :dateFrom
		  and je.entryDate <= :dateTo
		  and l.account.type = :accountType
	""")
	List<JournalEntryLine> findApprovedLinesByAccountTypeBetweenDates(
		AccountType accountType,
		java.time.LocalDate dateFrom,
		java.time.LocalDate dateTo
	);
}
