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
    @NotBlank(message = "Account code is required")
    @Size(min = 3, max = 50, message = "Account code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "Account code can only contain letters, numbers, hyphens, and underscores")
    private String code;

    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 200, message = "Account name must be between 2 and 200 characters")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Currency is required")
    private Currency currency;

    private String parentId;
}