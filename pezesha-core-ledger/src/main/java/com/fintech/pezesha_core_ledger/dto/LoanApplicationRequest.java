package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanApplicationRequest {
    private String accountId;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private int termInDays;
    private Currency currency;
    private LocalDateTime dueDate;
}

