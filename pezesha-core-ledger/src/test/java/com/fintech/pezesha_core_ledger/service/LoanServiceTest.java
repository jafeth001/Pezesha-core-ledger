package com.fintech.pezesha_core_ledger.service;

import com.fintech.pezesha_core_ledger.dto.*;
import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.enums.LoanStatus;
import com.fintech.pezesha_core_ledger.exception.ResourceNotFoundException;
import com.fintech.pezesha_core_ledger.exception.ValidationException;
import com.fintech.pezesha_core_ledger.models.Account;
import com.fintech.pezesha_core_ledger.models.Loan;
import com.fintech.pezesha_core_ledger.repository.AccountRepository;
import com.fintech.pezesha_core_ledger.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountRepository accountRepository;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, transactionService, accountRepository);
    }

    @Test
    void applyForLoan_validRequest_createsLoan() {
        // Arrange
        LoanApplicationRequest request = LoanApplicationRequest.builder()
                .accountId("acc-123")
                .principalAmount(new BigDecimal("10000"))
                .interestRate(new BigDecimal("0.10"))
                .currency(Currency.KES)
                .dueDate(LocalDateTime.now().plusMonths(12))
                .build();

        Account account = Account.builder()
                .id("acc-123")
                .build();

        when(accountRepository.findById("acc-123"))
                .thenReturn(Optional.of(account));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan saved = invocation.getArgument(0);
            saved.setId("loan-123");
            return saved;
        });

        // Act
        LoanResponse response = loanService.applyForLoan(request);

        // Assert
        assertNotNull(response);
        assertEquals(LoanStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("10000"), response.getPrincipalAmount());
        assertEquals("acc-123", response.getAccountId());
    }

    @Test
    void disburseLoan_validRequest_disbursesLoan() {
        // Arrange
        String loanId = "loan-123";
        DisbursementRequest request = DisbursementRequest.builder()
                .amount(new BigDecimal("5000"))
                .originationFee(new BigDecimal("50"))
                .currency(Currency.KES)
                .idempotencyKey("disb-key")
                .loansReceivableAccountId("loan-rec-acc")
                .cashAccountId("cash-acc")
                .origFeeReceivableAccountId("fee-rec-acc")
                .feeIncomeAccountId("fee-inc-acc")
                .build();

        Loan loan = Loan.builder()
                .id(loanId)
                .accountId("acc-123")
                .principalAmount(new BigDecimal("10000"))
                .outstandingBalance(new BigDecimal("10000"))
                .status(LoanStatus.APPROVED)
                .currency(Currency.KES)
                .build();

        Account loanAccount = Account.builder().id("acc-123").build();
        Account loansReceivableAccount = Account.builder().id("loan-rec-acc").build();
        Account cashAccount = Account.builder().id("cash-acc").build();
        Account origFeeReceivableAccount = Account.builder().id("fee-rec-acc").build();
        Account feeIncomeAccount = Account.builder().id("fee-inc-acc").build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(accountRepository.findById("acc-123")).thenReturn(Optional.of(loanAccount));
        when(accountRepository.findById("loan-rec-acc")).thenReturn(Optional.of(loansReceivableAccount));
        when(accountRepository.findById("cash-acc")).thenReturn(Optional.of(cashAccount));
        when(accountRepository.findById("fee-rec-acc")).thenReturn(Optional.of(origFeeReceivableAccount));
        when(accountRepository.findById("fee-inc-acc")).thenReturn(Optional.of(feeIncomeAccount));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan saved = invocation.getArgument(0);
            saved.setStatus(LoanStatus.DISBURSED);
            return saved;
        });

        // Act
        LoanResponse response = loanService.disburseLoan(loanId, request);

        // Assert
        assertNotNull(response);
        assertEquals(LoanStatus.DISBURSED, response.getStatus());
        assertEquals(new BigDecimal("5000"), response.getOutstandingBalance());

        verify(transactionService, times(2)).postTransaction(any(TransactionRequest.class));
    }

    @Test
    void repayLoan_validRequest_processesRepayment() {
        // Arrange
        String loanId = "loan-123";
        RepaymentRequest request = RepaymentRequest.builder()
                .amount(new BigDecimal("1000"))
                .principalPortion(new BigDecimal("800"))
                .interestPortion(new BigDecimal("200"))
                .currency(Currency.KES)
                .idempotencyKey("repay-key")
                .cashAccountId("cash-acc")
                .loansReceivableAccountId("loan-rec-acc")
                .interestIncomeAccountId("int-inc-acc")
                .build();

        Loan loan = Loan.builder()
                .id(loanId)
                .accountId("acc-123")
                .outstandingBalance(new BigDecimal("5000"))
                .status(LoanStatus.DISBURSED)
                .currency(Currency.KES)
                .build();

        Account loanAccount = Account.builder().id("acc-123").build();
        Account cashAccount = Account.builder().id("cash-acc").build();
        Account loansReceivableAccount = Account.builder().id("loan-rec-acc").build();
        Account interestIncomeAccount = Account.builder().id("int-inc-acc").build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(accountRepository.findById("acc-123")).thenReturn(Optional.of(loanAccount));
        when(accountRepository.findById("cash-acc")).thenReturn(Optional.of(cashAccount));
        when(accountRepository.findById("loan-rec-acc")).thenReturn(Optional.of(loansReceivableAccount));
        when(accountRepository.findById("int-inc-acc")).thenReturn(Optional.of(interestIncomeAccount));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan saved = invocation.getArgument(0);
            saved.setOutstandingBalance(new BigDecimal("4200"));
            return saved;
        });

        // Act
        LoanResponse response = loanService.repayLoan(loanId, request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("4200"), response.getOutstandingBalance());

        verify(transactionService).postTransaction(any(TransactionRequest.class));
    }

    @Test
    void getLoan_loanNotExists_throwsResourceNotFoundException() {
        // Arrange
        String loanId = "non-existent";

        when(loanRepository.findById(loanId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> loanService.repayLoan(loanId, RepaymentRequest.builder().build()));
    }

    @Test
    void writeOffLoan_validRequest_writesOffLoan() {
        // Arrange
        String loanId = "loan-123";
        WriteOffRequest request = WriteOffRequest.builder()
                .amount(new BigDecimal("1000"))
                .currency(Currency.KES)
                .idempotencyKey("writeoff-key")
                .badDebtExpenseAccountId("bad-debt-acc")
                .loansReceivableAccountId("loan-rec-acc")
                .build();

        Loan loan = Loan.builder()
                .id(loanId)
                .accountId("acc-123")
                .outstandingBalance(new BigDecimal("1000"))
                .status(LoanStatus.DISBURSED)
                .currency(Currency.KES)
                .build();

        Account badDebtAccount = Account.builder().id("bad-debt-acc").build();
        Account loansReceivableAccount = Account.builder().id("loan-rec-acc").build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(accountRepository.findById("bad-debt-acc")).thenReturn(Optional.of(badDebtAccount));
        when(accountRepository.findById("loan-rec-acc")).thenReturn(Optional.of(loansReceivableAccount));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan saved = invocation.getArgument(0);
            saved.setStatus(LoanStatus.WRITTEN_OFF);
            saved.setOutstandingBalance(BigDecimal.ZERO);
            return saved;
        });

        // Act
        LoanResponse response = loanService.writeOffLoan(loanId, request);

        // Assert
        assertNotNull(response);
        assertEquals(LoanStatus.WRITTEN_OFF, response.getStatus());
        assertEquals(BigDecimal.ZERO, response.getOutstandingBalance());

        verify(transactionService).postTransaction(any(TransactionRequest.class));
    }
}
