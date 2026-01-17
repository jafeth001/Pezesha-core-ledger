package com.fintech.pezesha_core_ledger.repository;

import com.fintech.pezesha_core_ledger.enums.LoanStatus;
import com.fintech.pezesha_core_ledger.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {
    List<Loan> findByStatusIn(List<LoanStatus> list);
}
