package com.fintech.pezesha_core_ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAgingResponse {
    private Map<String, LoanAgingBucket> buckets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanAgingBucket {
        private long count;
        private BigDecimal totalAmount;
    }
}
