package com.fintech.pezesha_core_ledger.service;

import com.fintech.pezesha_core_ledger.dto.BalanceSheetResponse;
import com.fintech.pezesha_core_ledger.dto.TrialBalanceResponse;
import com.fintech.pezesha_core_ledger.enums.AccountType;
import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.enums.LoanStatus;
import com.fintech.pezesha_core_ledger.models.Account;
import com.fintech.pezesha_core_ledger.models.Loan;
import com.fintech.pezesha_core_ledger.repository.AccountRepository;
import com.fintech.pezesha_core_ledger.repository.LoanRepository;
import com.fintech.pezesha_core_ledger.repository.TransactionEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private TransactionEntryRepository transactionEntryRepository;

    private ReportingService reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService(accountRepository, loanRepository, transactionEntryRepository);
    }

    @Test
    void getTrialBalance_validRequest_returnsTrialBalance() {
        // Arrange
        LocalDateTime asOfDate = LocalDateTime.now();

        Account assetAccount = Account.builder()
                .id("asset-123")
                .code("ASSET001")
                .name("Asset Account")
                .type(AccountType.ASSET)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        Account liabilityAccount = Account.builder()
                .id("liab-123")
                .code("LIAB001")
                .name("Liability Account")
                .type(AccountType.LIABILITY)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        List<Account> accounts = Arrays.asList(assetAccount, liabilityAccount);

        when(accountRepository.findByIsActiveTrue()).thenReturn(accounts);
        when(transactionEntryRepository.getAccountBalanceAsOf("asset-123", asOfDate))
                .thenReturn(new BigDecimal("1000"));
        when(transactionEntryRepository.getAccountBalanceAsOf("liab-123", asOfDate))
                .thenReturn(new BigDecimal("500"));

        // Act
        TrialBalanceResponse response = reportingService.getTrialBalance(asOfDate);

        // Assert
        assertNotNull(response);
        assertEquals(asOfDate, response.getAsOfDate());

        // Verify account type summaries contain expected values
        TrialBalanceResponse.AccountTypeSummary assetSummary = response.getAccountTypeSummaries().get(AccountType.ASSET);
        TrialBalanceResponse.AccountTypeSummary liabilitySummary = response.getAccountTypeSummaries().get(AccountType.LIABILITY);

        assertNotNull(assetSummary);
        assertNotNull(liabilitySummary);
    }

    @Test
    void getBalanceSheet_validRequest_returnsBalanceSheet() {
        // Arrange
        LocalDateTime asOfDate = LocalDateTime.now();

        Account assetAccount = Account.builder()
                .id("asset-123")
                .code("ASSET001")
                .name("Asset Account")
                .type(AccountType.ASSET)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        Account liabilityAccount = Account.builder()
                .id("liab-123")
                .code("LIAB001")
                .name("Liability Account")
                .type(AccountType.LIABILITY)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        Account equityAccount = Account.builder()
                .id("equity-123")
                .code("EQUITY001")
                .name("Equity Account")
                .type(AccountType.EQUITY)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        List<Account> accounts = Arrays.asList(assetAccount, liabilityAccount, equityAccount);

        when(accountRepository.findByIsActiveTrue()).thenReturn(accounts);
        when(transactionEntryRepository.getAccountBalanceAsOf("asset-123", asOfDate))
                .thenReturn(new BigDecimal("1000"));
        when(transactionEntryRepository.getAccountBalanceAsOf("liab-123", asOfDate))
                .thenReturn(new BigDecimal("500"));
        when(transactionEntryRepository.getAccountBalanceAsOf("equity-123", asOfDate))
                .thenReturn(new BigDecimal("500"));

        // Act
        BalanceSheetResponse response = reportingService.getBalanceSheet(asOfDate);

        // Assert
        assertNotNull(response);
        assertEquals(asOfDate, response.getAsOfDate());
        assertTrue(response.isBalanced());

        assertEquals(new BigDecimal("1000"), response.getTotalAssets());
        assertEquals(new BigDecimal("500"), response.getTotalLiabilities());
        assertEquals(new BigDecimal("500"), response.getTotalEquity());
    }

    @Test
    void getBalanceSheet_withIncomeAndExpense_calculatesNetIncome() {
        // Arrange
        LocalDateTime asOfDate = LocalDateTime.now();

        Account incomeAccount = Account.builder()
                .id("income-123")
                .code("INCOME001")
                .name("Income Account")
                .type(AccountType.INCOME)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        Account expenseAccount = Account.builder()
                .id("expense-123")
                .code("EXPENSE001")
                .name("Expense Account")
                .type(AccountType.EXPENSE)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        List<Account> accounts = Arrays.asList(incomeAccount, expenseAccount);

        when(accountRepository.findByIsActiveTrue()).thenReturn(accounts);
        when(transactionEntryRepository.getAccountBalanceAsOf("income-123", asOfDate))
                .thenReturn(new BigDecimal("1000"));
        when(transactionEntryRepository.getAccountBalanceAsOf("expense-123", asOfDate))
                .thenReturn(new BigDecimal("400"));

        // Act
        BalanceSheetResponse response = reportingService.getBalanceSheet(asOfDate);

        // Assert
        assertNotNull(response);
    }
}
