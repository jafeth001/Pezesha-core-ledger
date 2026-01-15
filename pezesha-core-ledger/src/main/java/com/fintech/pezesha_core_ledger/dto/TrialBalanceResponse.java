package com.fintech.pezesha_core_ledger.dto;

import com.fintech.pezesha_core_ledger.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class TrialBalanceResponse {
    private LocalDateTime asOfDate;
    private Map<AccountType, AccountTypeSummary> accountTypeSummaries;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private Boolean isBalanced;

    @Data
    @Builder
    public static class AccountTypeSummary {
        private BigDecimal totalDebits;
        private BigDecimal totalCredits;
    }

    @Data
    @Builder
    public static class AccountBalanceDetail {
        private String accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal balance;
    }
}
