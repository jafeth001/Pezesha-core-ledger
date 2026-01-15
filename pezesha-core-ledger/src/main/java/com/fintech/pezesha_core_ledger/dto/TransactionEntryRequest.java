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
    @NotBlank
    private String accountId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal debit;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal credit;

    @NotNull
    private Currency currency;
}
