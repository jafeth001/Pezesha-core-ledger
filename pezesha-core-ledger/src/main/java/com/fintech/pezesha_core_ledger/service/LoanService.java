package com.fintech.pezesha_core_ledger.service;

import com.fintech.pezesha_core_ledger.dto.*;
import com.fintech.pezesha_core_ledger.enums.LoanStatus;
import com.fintech.pezesha_core_ledger.exception.ResourceNotFoundException;
import com.fintech.pezesha_core_ledger.exception.ValidationException;
import com.fintech.pezesha_core_ledger.models.Account;
import com.fintech.pezesha_core_ledger.models.Loan;
import com.fintech.pezesha_core_ledger.repository.AccountRepository;
import com.fintech.pezesha_core_ledger.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;

    // Apply for a loan
    @Transactional
    public LoanResponse applyForLoan(LoanApplicationRequest req) {
        Account account = accountRepository.findById(req.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + req.getAccountId()));

        Loan loan = Loan.builder()
                .accountId(req.getAccountId())
                .principalAmount(req.getPrincipalAmount())
                .interestRate(req.getInterestRate())
                .currency(req.getCurrency())
                .dueDate(req.getDueDate())
                .outstandingBalance(req.getPrincipalAmount())
                .status(LoanStatus.PENDING)
                .build();

        return toResponse(loanRepository.save(loan));
    }

    public LoanResponse approveLoan(String loanId) {
        Loan loan = getLoan(loanId);
        assertStatus(loan, LoanStatus.PENDING);

        loan.setStatus(LoanStatus.APPROVED);
        return toResponse(loanRepository.save(loan));
    }

    // Disburse loan
    @Transactional
    public LoanResponse disburseLoan(String loanId, DisbursementRequest req) {
        Loan loan = getLoan(loanId);
        assertStatus(loan, LoanStatus.APPROVED);

        // Get account details for the loan
        Account loanAccount = accountRepository.findById(loan.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan account not found: " + loan.getAccountId()));

        // Get other required accounts using IDs instead of codes
        Account loansReceivableAccount = accountRepository.findById(req.getLoansReceivableAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Loans receivable account not found: " + req.getLoansReceivableAccountId()));

        Account cashAccount = accountRepository.findById(req.getCashAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Cash account not found: " + req.getCashAccountId()));

        Account origFeeReceivableAccount = accountRepository.findById(req.getOrigFeeReceivableAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Origination fee receivable account not found: " + req.getOrigFeeReceivableAccountId()));

        Account feeIncomeAccount = accountRepository.findById(req.getFeeIncomeAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee income account not found: " + req.getFeeIncomeAccountId()));

        // Create transaction entries for disbursement
        List<TransactionEntryRequest> entries = Arrays.asList(
                // Debit loan account (increase customer's balance)
                TransactionEntryRequest.builder()
                        .accountId(loan.getAccountId())
                        .debit(req.getAmount())
                        .credit(BigDecimal.ZERO)
                        .currency(req.getCurrency())
                        .build(),
                // Credit loans receivable account
                TransactionEntryRequest.builder()
                        .accountId(loansReceivableAccount.getId())
                        .debit(BigDecimal.ZERO)
                        .credit(req.getAmount())
                        .currency(req.getCurrency())
                        .build()
        );

        // Create transaction for origination fee
        List<TransactionEntryRequest> feeEntries = Arrays.asList(
                // Debit origination fee receivable account
                TransactionEntryRequest.builder()
                        .accountId(origFeeReceivableAccount.getId())
                        .debit(req.getOriginationFee())
                        .credit(BigDecimal.ZERO)
                        .currency(req.getCurrency())
                        .build(),
                // Credit fee income account
                TransactionEntryRequest.builder()
                        .accountId(feeIncomeAccount.getId())
                        .debit(BigDecimal.ZERO)
                        .credit(req.getOriginationFee())
                        .currency(req.getCurrency())
                        .build()
        );

        // Post disbursement transaction
        transactionService.postTransaction(
                TransactionRequest.builder()
                        .idempotencyKey(req.getIdempotencyKey() + "_disbursement")
                        .description("Loan disbursement " + loanId)
                        .entries(entries)
                        .build()
        );

        // Post origination fee transaction
        transactionService.postTransaction(
                TransactionRequest.builder()
                        .idempotencyKey(req.getIdempotencyKey() + "_fee")
                        .description("Loan origination fee " + loanId)
                        .entries(feeEntries)
                        .build()
        );

        loan.setOutstandingBalance(req.getAmount());
        loan.setDisbursementDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.DISBURSED);

        return toResponse(loanRepository.save(loan));
    }

    // repay loan
    @Transactional
    public LoanResponse repayLoan(String loanId, RepaymentRequest req) {
        Loan loan = getLoan(loanId);

        // Get required accounts using IDs
        Account loanAccount = accountRepository.findById(loan.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan account not found: " + loan.getAccountId()));

        Account cashAccount = accountRepository.findById(req.getCashAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Cash account not found: " + req.getCashAccountId()));

        Account loansReceivableAccount = accountRepository.findById(req.getLoansReceivableAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Loans receivable account not found: " + req.getLoansReceivableAccountId()));

        Account interestIncomeAccount = accountRepository.findById(req.getInterestIncomeAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Interest income account not found: " + req.getInterestIncomeAccountId()));

        // Create transaction entries for repayment
        List<TransactionEntryRequest> entries = Arrays.asList(
                // Debit cash account (decrease customer's cash)
                TransactionEntryRequest.builder()
                        .accountId(cashAccount.getId())
                        .debit(req.getAmount())
                        .credit(BigDecimal.ZERO)
                        .currency(req.getCurrency())
                        .build(),
                // Credit loan account (decrease customer's loan balance)
                TransactionEntryRequest.builder()
                        .accountId(loan.getAccountId())
                        .debit(BigDecimal.ZERO)
                        .credit(req.getPrincipalPortion())
                        .currency(req.getCurrency())
                        .build(),
                // Credit interest income account
                TransactionEntryRequest.builder()
                        .accountId(interestIncomeAccount.getId())
                        .debit(BigDecimal.ZERO)
                        .credit(req.getInterestPortion())
                        .currency(req.getCurrency())
                        .build()
        );

        transactionService.postTransaction(
                TransactionRequest.builder()
                        .idempotencyKey(req.getIdempotencyKey())
                        .description("Loan repayment " + loanId)
                        .entries(entries)
                        .build()
        );

        loan.setOutstandingBalance(
                loan.getOutstandingBalance().subtract(req.getPrincipalPortion())
        );
        loan.setLastPaymentDate(LocalDateTime.now());

        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.CLOSED);
        }

        return toResponse(loanRepository.save(loan));
    }

    // Write off loan
    @Transactional
    public LoanResponse writeOffLoan(String loanId, WriteOffRequest req) {
        Loan loan = getLoan(loanId);

        // Get required accounts using IDs
        Account badDebtExpenseAccount = accountRepository.findById(req.getBadDebtExpenseAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Bad debt expense account not found: " + req.getBadDebtExpenseAccountId()));

        Account loansReceivableAccount = accountRepository.findById(req.getLoansReceivableAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Loans receivable account not found: " + req.getLoansReceivableAccountId()));

        List<TransactionEntryRequest> entries = Arrays.asList(
                // Debit bad debt expense account
                TransactionEntryRequest.builder()
                        .accountId(badDebtExpenseAccount.getId())
                        .debit(req.getAmount())
                        .credit(BigDecimal.ZERO)
                        .currency(req.getCurrency())
                        .build(),
                // Credit loans receivable account
                TransactionEntryRequest.builder()
                        .accountId(loansReceivableAccount.getId())
                        .debit(BigDecimal.ZERO)
                        .credit(req.getAmount())
                        .currency(req.getCurrency())
                        .build()
        );

        transactionService.postTransaction(
                TransactionRequest.builder()
                        .idempotencyKey(req.getIdempotencyKey())
                        .description("Loan write-off " + loanId)
                        .entries(entries)
                        .build()
        );

        loan.setOutstandingBalance(BigDecimal.ZERO);
        loan.setStatus(LoanStatus.WRITTEN_OFF);

        return toResponse(loanRepository.save(loan));
    }

    public List<LoanResponse> getAllLoans() {
        return loanRepository.findByStatusIn(Arrays.asList(LoanStatus.APPROVED, LoanStatus.DISBURSED))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // HELPERS

    private Loan getLoan(String loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }

    private void assertStatus(Loan loan, LoanStatus expected) {
        if (loan.getStatus() != expected) {
            throw new ValidationException(
                    "Loan must be " + expected + " but is " + loan.getStatus()
            );
        }
    }

    private LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .loanId(loan.getId())
                .accountId(loan.getAccountId())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .currency(loan.getCurrency())
                .outstandingBalance(loan.getOutstandingBalance())
                .status(loan.getStatus())
                .disbursementDate(loan.getDisbursementDate())
                .dueDate(loan.getDueDate())
                .lastPaymentDate(loan.getLastPaymentDate())
                .createdAt(loan.getCreatedAt())
                .build();
    }

}
