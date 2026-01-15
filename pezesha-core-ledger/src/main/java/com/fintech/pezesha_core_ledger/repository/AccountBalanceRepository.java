package com.fintech.pezesha_core_ledger.repository;

import com.fintech.pezesha_core_ledger.dto.AccountBalanceDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountBalanceRepository extends Repository<Object, String> {

    @Query("""
        SELECT new com.fintech.ledger.dto.response.AccountBalanceDetail(
            a.id,
            a.code,
            a.name,
            COALESCE(SUM(te.debit - te.credit), 0)
        )
        FROM Account a
        LEFT JOIN TransactionEntry te ON a.id = te.account.id
        LEFT JOIN Transaction t ON te.transaction.id = t.id
        WHERE t.status = 'POSTED' 
        AND (te.postedAt <= :asOfDate OR te.postedAt IS NULL)
        GROUP BY a.id, a.code, a.name
        HAVING COALESCE(SUM(te.debit - te.credit), 0) != 0
        ORDER BY a.code
        """)
    List<AccountBalanceDetail> getAllAccountBalancesAsOf(@Param("asOfDate") LocalDateTime asOfDate);

    @Query(value = """
        SELECT * FROM account_daily_balances 
        WHERE balance_date = :date 
        ORDER BY account_code
        """, nativeQuery = true)
    List<Object[]> getDailyBalances(@Param("date") String date);
}
