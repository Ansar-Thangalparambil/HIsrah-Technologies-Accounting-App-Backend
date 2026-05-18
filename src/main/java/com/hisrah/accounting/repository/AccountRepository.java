package com.hisrah.accounting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hisrah.accounting.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

	boolean existsByCode(String code);

	boolean existsByCodeAndIdNot(String code, Long id);

	List<Account> findAllByOrderByCodeAsc();

	List<Account> findByParentIsNullAndActiveTrueOrderByCodeAsc();
}
