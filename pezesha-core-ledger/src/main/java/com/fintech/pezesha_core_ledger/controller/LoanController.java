package com.fintech.pezesha_core_ledger.controller;

import com.fintech.pezesha_core_ledger.dto.*;
import com.fintech.pezesha_core_ledger.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Management", description = "Endpoints for loan lifecycle operations")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Apply for a new loan",  description = "Apply for a new loan")
    public ResponseEntity<LoanResponse> apply(@Valid @RequestBody LoanApplicationRequest req) {
        return ResponseEntity.ok(loanService.applyForLoan(req));
    }

    @PostMapping("/{loanId}/disburse")
    @Operation(summary = "Disburse a loan",  description = "Disburse a loan")
    public ResponseEntity<LoanResponse> disburse(
            @PathVariable String loanId,
            @Valid @RequestBody DisbursementRequest req) {
        return ResponseEntity.ok(loanService.disburseLoan(loanId, req));
    }

    @PostMapping("/{loanId}/repay")
    @Operation(summary = "Repay a loan",  description = "Repay a loan")
    public ResponseEntity<LoanResponse> repay(
            @PathVariable String loanId,
            @Valid @RequestBody RepaymentRequest req) {
        return ResponseEntity.ok(loanService.repayLoan(loanId, req));
    }

    @PostMapping("/{loanId}/write-off")
    @Operation(summary = "Write off a loan",  description = "Write off a loan")
    public ResponseEntity<LoanResponse> writeOff(
            @PathVariable String loanId,
            @Valid @RequestBody WriteOffRequest req) {
        return ResponseEntity.ok(loanService.writeOffLoan(loanId, req));
    }

    @GetMapping
    @Operation(summary = "Get all loans",  description = "Retrieves a list of all loans")
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @PutMapping("/{loanId}/approve")
    @Operation(summary = "Approve a loan", description = "Approve a loan for disbursement")
    public ResponseEntity<LoanResponse> approveLoan(@PathVariable String loanId) {
        return ResponseEntity.ok(loanService.approveLoan(loanId));
    }
}
