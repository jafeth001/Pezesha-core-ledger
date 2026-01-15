package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.AccountType;
import com.fintech.pezesha_core_ledger.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String id;
    private String code;
    private String name;
    private AccountType type;
    private Currency currency;
    private String parentId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}