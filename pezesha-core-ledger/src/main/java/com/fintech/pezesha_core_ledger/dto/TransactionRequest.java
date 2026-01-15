package com.fintech.pezesha_core_ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    @NotBlank
    private String idempotencyKey;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    @Size(min = 2)
    private List<TransactionEntryRequest> entries;

    private Map<String, Object> metadata;
}
