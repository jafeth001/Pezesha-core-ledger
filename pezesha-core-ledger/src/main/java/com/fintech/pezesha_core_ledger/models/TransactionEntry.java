package com.fintech.pezesha_core_ledger.models;

import com.fintech.pezesha_core_ledger.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * This is where money actually moves.
 */

@Entity
@Table(name = "transaction_entries", indexes = {
        @Index(name = "idx_entry_account_date", columnList = "account_id, posted_at"),
        @Index(name = "idx_entry_transaction", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal credit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(name = "running_balance")
    private BigDecimal runningBalance;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;
}