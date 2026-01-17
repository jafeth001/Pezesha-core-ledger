package com.fintech.pezesha_core_ledger.models;

import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "loans",
        indexes = {
                @Index(name = "idx_loan_account", columnList = "account_id"),
                @Index(name = "idx_loan_status", columnList = "status"),
                @Index(name = "idx_loan_due_date", columnList = "due_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(name = "outstanding_balance")
    private BigDecimal outstandingBalance;

    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate;

    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
