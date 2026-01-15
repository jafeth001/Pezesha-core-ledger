package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {
    private String accountId;
    private String accountCode;
    private String accountName;
    private Currency currency;
    private BigDecimal balance;
    private LocalDateTime asOfDate;
}