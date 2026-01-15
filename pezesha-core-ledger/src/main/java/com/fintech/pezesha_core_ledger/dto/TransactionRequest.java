package com.fintech.pezesha_core_ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Transaction entries are required")
    @Size(min = 2, message = "Transaction must have at least 2 entries")
    private List<TransactionEntryRequest> entries;
}
