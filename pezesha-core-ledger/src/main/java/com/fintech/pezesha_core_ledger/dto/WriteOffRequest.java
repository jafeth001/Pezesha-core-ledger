package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriteOffRequest {
    @NotBlank
    private String idempotencyKey;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private Currency currency;

    @NotBlank
    private String badDebtExpenseAccountId;

    @NotBlank
    private String loansReceivableAccountId;
}