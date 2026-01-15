package com.fintech.pezesha_core_ledger.repository;

import com.fintech.pezesha_core_ledger.enums.AccountType;
import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByCode(String code);

    List<Account> findByTypeAndCurrency(AccountType type, Currency currency);

    List<Account> findByParentIdAndIsActiveTrue(String parentId);

    List<Account> findByIsActiveTrue();

    @Query("SELECT a FROM Account a WHERE a.code LIKE :prefix% AND a.isActive = true ORDER BY a.code")
    List<Account> findByCodePrefix(@Param("prefix") String prefix);
}