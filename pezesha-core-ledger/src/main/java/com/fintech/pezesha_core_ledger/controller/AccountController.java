package com.fintech.pezesha_core_ledger.controller;

import com.fintech.pezesha_core_ledger.dto.AccountBalanceResponse;
import com.fintech.pezesha_core_ledger.dto.AccountResponse;
import com.fintech.pezesha_core_ledger.dto.CreateAccountRequest;
import com.fintech.pezesha_core_ledger.enums.AccountType;
import com.fintech.pezesha_core_ledger.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing ledger accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account", description = "Creates a new ledger account with specified parameters")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID", description = "Retrieves account details by its unique identifier")
    public ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "Unique identifier of the account", required = true)
            @PathVariable String accountId) {
        AccountResponse response = accountService.getAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Retrieves the current balance of an account as of a specific date")
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(
            @Parameter(description = "Unique identifier of the account", required = true)
            @PathVariable String accountId,
            @Parameter(description = "Date to retrieve balance as of (defaults to current date if not provided)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime asOfDate) {

        AccountBalanceResponse response = accountService.getAccountBalance(accountId, asOfDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all active accounts", description = "Retrieves all active accounts in the ledger system")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> response = accountService.getAllAccounts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{accountType}")
    @Operation(summary = "Get accounts by type", description = "Retrieves all accounts of a specific account type")
    public ResponseEntity<List<AccountResponse>> getAccountsByType(
            @Parameter(description = "Type of account to filter by", required = true)
            @PathVariable AccountType accountType) {
        List<AccountResponse> response = accountService.getAccountsByType(accountType);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Deactivate account", description = "Deactivates an account by setting its active status to false")
    public ResponseEntity<AccountResponse> deactivateAccount(
            @Parameter(description = "Unique identifier of the account to deactivate", required = true)
            @PathVariable String accountId) {
        AccountResponse response = accountService.deactivateAccount(accountId);
        return ResponseEntity.ok(response);
    }
}
