package com.fintech.pezesha_core_ledger.repository;

import com.fintech.pezesha_core_ledger.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'POSTED' AND t.postedAt BETWEEN :startDate AND :endDate ORDER BY t.postedAt DESC")
    Page<Transaction> findPostedTransactionsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t JOIN t.entries te WHERE te.account.id = :accountId AND t.status = 'POSTED' AND t.postedAt BETWEEN :startDate AND :endDate ORDER BY t.postedAt DESC")
    Page<Transaction> findByAccountAndDateRange(
            @Param("accountId") String accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
