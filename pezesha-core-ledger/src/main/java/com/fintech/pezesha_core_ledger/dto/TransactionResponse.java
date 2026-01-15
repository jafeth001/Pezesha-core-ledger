package com.fintech.pezesha_core_ledger.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TransactionResponse {
    private String id;
    private String idempotencyKey;
    private String description;
    private String status;
    private LocalDateTime postedAt;
    private List<TransactionEntryResponse> entries;
    private LocalDateTime createdAt;
}
