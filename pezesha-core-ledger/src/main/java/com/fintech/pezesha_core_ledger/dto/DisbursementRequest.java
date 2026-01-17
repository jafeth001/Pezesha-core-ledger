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
public class DisbursementRequest {
    @NotBlank
    private String idempotencyKey;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private BigDecimal originationFee;

    @NotNull
    private Currency currency;

    @NotBlank
    private String loansReceivableAccountId;

    @NotBlank
    private String cashAccountId;

    @NotBlank
    private String origFeeReceivableAccountId;

    @NotBlank
    private String feeIncomeAccountId;
}
