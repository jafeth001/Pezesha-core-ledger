package com.fintech.pezesha_core_ledger.repository;

import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.models.TransactionEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, String> {


    @Query("SELECT te FROM TransactionEntry te WHERE te.account.id = :accountId AND te.transaction.status = 'POSTED' ORDER BY te.postedAt, te.createdAt")
    List<TransactionEntry> findByAccountId(@Param("accountId") String accountId);

    @Query("SELECT COALESCE(SUM(te.debit - te.credit), 0) FROM TransactionEntry te WHERE te.account.id = :accountId AND te.transaction.status = 'POSTED' AND te.postedAt <= :asOfDate")
    BigDecimal getAccountBalanceAsOf(@Param("accountId") String accountId, @Param("asOfDate") LocalDateTime asOfDate);

    @Query("SELECT te FROM TransactionEntry te WHERE te.account.id = :accountId AND te.transaction.status = 'POSTED' AND te.postedAt <= :asOfDate ORDER BY te.postedAt, te.createdAt")
    List<TransactionEntry> getAccountEntriesAsOf(
            @Param("accountId") String accountId,
            @Param("asOfDate") LocalDateTime asOfDate
    );
}
