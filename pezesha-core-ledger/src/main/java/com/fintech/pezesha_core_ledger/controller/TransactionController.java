package com.fintech.pezesha_core_ledger.controller;

import com.fintech.pezesha_core_ledger.dto.ReverseTransactionRequest;
import com.fintech.pezesha_core_ledger.dto.TransactionRequest;
import com.fintech.pezesha_core_ledger.dto.TransactionResponse;
import com.fintech.pezesha_core_ledger.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing financial transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Post a new transaction", description = "Creates and posts a new financial transaction to the ledger")
    public ResponseEntity<TransactionResponse> postTransaction(
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.postTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction by its unique identifier")
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "Unique identifier of the transaction", required = true)
            @PathVariable String transactionId) {
        TransactionResponse response = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieves a list of all transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> responses = transactionService.getAllTransactions();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{transactionId}/reverse")
    @Operation(summary = "Reverse a transaction", description = "Creates a reversing transaction for the specified transaction")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @Parameter(description = "Unique identifier of the transaction to reverse", required = true)
            @PathVariable String transactionId,
            @Valid @RequestBody ReverseTransactionRequest request) {

        TransactionResponse response = transactionService.reverseTransaction(transactionId, request.getReason());
        return ResponseEntity.ok(response);
    }
}
