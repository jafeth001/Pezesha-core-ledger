package com.fintech.pezesha_core_ledger.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BalanceSheetResponse {
    
    private LocalDateTime asOfDate;
    private AccountTypeSummary assets;
    private AccountTypeSummary liabilities;
    private AccountTypeSummary equity;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    private boolean isBalanced;

    @Data
    @Builder
    public static class AccountTypeSummary {
        private List<AccountBalance> accounts;
    }

    @Data
    @Builder 
    public static class AccountBalance {
        private String accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal balance;
    }
}
