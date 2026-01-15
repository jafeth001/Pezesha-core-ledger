package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
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
public class TransactionEntryRequest {
    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotNull(message = "Debit amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Debit amount must be positive")
    private BigDecimal debit;

    @NotNull(message = "Credit amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Credit amount must be positive")
    private BigDecimal credit;

    @NotNull(message = "Currency is required")
    private Currency currency;
}
