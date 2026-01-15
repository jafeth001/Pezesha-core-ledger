package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.AccountType;
import com.fintech.pezesha_core_ledger.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String code;

    @NotBlank
    @Size(min = 2, max = 200)
    private String name;

    @NotNull
    private AccountType type;

    @NotNull
    private Currency currency;

    private String parentId;
}