package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
class TransactionEntryResponse {
    private String accountId;
    private String accountCode;
    private BigDecimal debit;
    private BigDecimal credit;
    private Currency currency;
    private BigDecimal runningBalance;
}