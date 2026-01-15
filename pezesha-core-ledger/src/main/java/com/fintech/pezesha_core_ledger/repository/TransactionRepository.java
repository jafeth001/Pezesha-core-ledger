package com.fintech.pezesha_core_ledger.repository;

import com.fintech.pezesha_core_ledger.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
