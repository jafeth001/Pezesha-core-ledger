package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.enums.LoanStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private String loanId;
    private String accountId;

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Currency currency;

    private BigDecimal outstandingBalance;
    private LoanStatus status;

    private LocalDateTime disbursementDate;
    private LocalDateTime dueDate;
    private LocalDateTime lastPaymentDate;

    private LocalDateTime createdAt;
}

