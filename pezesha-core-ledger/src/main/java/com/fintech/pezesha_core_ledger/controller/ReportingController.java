package com.fintech.pezesha_core_ledger.controller;

import com.fintech.pezesha_core_ledger.dto.BalanceSheetResponse;
import com.fintech.pezesha_core_ledger.dto.LoanAgingResponse;
import com.fintech.pezesha_core_ledger.dto.TransactionEntryResponse;
import com.fintech.pezesha_core_ledger.dto.TrialBalanceResponse;
import com.fintech.pezesha_core_ledger.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "APIs for generating financial reports")
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/trial-balance")
    @Operation(summary = "Get trial balance report", description = "Retrieves the trial balance report showing all account balances")
    public ResponseEntity<TrialBalanceResponse> getTrialBalance(
            @Parameter(description = "Date to retrieve trial balance as of (defaults to current date if not provided)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime asOfDate) {

        TrialBalanceResponse response = reportingService.getTrialBalance(asOfDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance-sheet")
    @Operation(summary = "Get balance sheet report", description = "Retrieves the balance sheet showing assets, liabilities, and equity")
    public ResponseEntity<BalanceSheetResponse> getBalanceSheet(
            @Parameter(description = "Date to retrieve balance sheet as of (defaults to current date if not provided)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime asOfDate) {

        BalanceSheetResponse response = reportingService.getBalanceSheet(asOfDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/loan-aging")
    @Operation(summary = "Get loan aging report", description = "Retrieves the loan aging report categorizing loans by days overdue")
    public ResponseEntity<LoanAgingResponse> getLoanAgingReport() {
        LoanAgingResponse response = reportingService.getLoanAgingReport();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}/history")
    @Operation(summary = "Get transaction history for account", description = "Retrieves paginated transaction history for a specific account")
    public ResponseEntity<Page<TransactionEntryResponse>> getTransactionHistory(
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {

        Page<TransactionEntryResponse> response = reportingService.getTransactionHistory(
                accountId, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

}
