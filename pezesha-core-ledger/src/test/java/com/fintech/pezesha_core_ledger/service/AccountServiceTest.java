package com.fintech.pezesha_core_ledger.service;

import com.fintech.pezesha_core_ledger.dto.AccountBalanceResponse;
import com.fintech.pezesha_core_ledger.dto.AccountResponse;
import com.fintech.pezesha_core_ledger.dto.CreateAccountRequest;
import com.fintech.pezesha_core_ledger.enums.AccountType;
import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.exception.ResourceNotFoundException;
import com.fintech.pezesha_core_ledger.exception.ValidationException;
import com.fintech.pezesha_core_ledger.models.Account;
import com.fintech.pezesha_core_ledger.repository.AccountRepository;
import com.fintech.pezesha_core_ledger.repository.TransactionEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionEntryRepository transactionEntryRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, transactionEntryRepository);
    }

    @Test
    void createAccount_validRequest_createsAccount() {
        // Arrange
        CreateAccountRequest request = CreateAccountRequest.builder()
                .code("NEW_ACCOUNT")
                .name("New Account")
                .type(AccountType.ASSET)
                .currency(Currency.KES)
                .build();

        when(accountRepository.findByCode("NEW_ACCOUNT"))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId("acc-123");
            return saved;
        });

        // Act
        AccountResponse response = accountService.createAccount(request);

        // Assert
        assertNotNull(response);
        assertEquals("NEW_ACCOUNT", response.getCode());
        assertEquals("New Account", response.getName());
        assertEquals(AccountType.ASSET, response.getType());

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_duplicateCode_throwsValidationException() {
        // Arrange
        CreateAccountRequest request = CreateAccountRequest.builder()
                .code("EXISTING_CODE")
                .name("New Account")
                .type(AccountType.ASSET)
                .currency(Currency.KES)
                .build();

        when(accountRepository.findByCode("EXISTING_CODE"))
                .thenReturn(Optional.of(new Account()));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> accountService.createAccount(request));
        assertTrue(exception.getMessage().contains("Account code already exists"));
    }

    @Test
    void getAccount_accountExists_returnsAccount() {
        // Arrange
        String accountId = "acc-123";
        Account account = Account.builder()
                .id(accountId)
                .code("TEST_CODE")
                .name("Test Account")
                .type(AccountType.LIABILITY)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        // Act
        AccountResponse response = accountService.getAccount(accountId);

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getId());
        assertEquals("TEST_CODE", response.getCode());
        assertEquals(AccountType.LIABILITY, response.getType());
    }

    @Test
    void getAccount_accountNotExists_throwsResourceNotFoundException() {
        // Arrange
        String accountId = "non-existent";

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.getAccount(accountId));
    }

    @Test
    void getAccountBalance_accountExists_returnsBalance() {
        // Arrange
        String accountId = "acc-123";
        BigDecimal expectedBalance = new BigDecimal("5000.00");
        LocalDateTime asOfDate = LocalDateTime.now();

        Account account = Account.builder()
                .id(accountId)
                .code("TEST_CODE")
                .name("Test Account")
                .type(AccountType.ASSET)
                .currency(Currency.KES)
                .build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));
        when(transactionEntryRepository.getAccountBalanceAsOf(accountId, asOfDate))
                .thenReturn(expectedBalance);

        // Act
        AccountBalanceResponse response = accountService.getAccountBalance(accountId, asOfDate);

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getAccountId());
        assertEquals(expectedBalance, response.getBalance());
        assertEquals(asOfDate, response.getAsOfDate());
    }

    @Test
    void deactivateAccount_nonZeroBalance_throwsValidationException() {
        // Arrange
        String accountId = "acc-123";
        BigDecimal nonZeroBalance = new BigDecimal("100.00");

        Account account = Account.builder()
                .id(accountId)
                .isActive(true)
                .build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));
        when(transactionEntryRepository.getAccountBalanceAsOf(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(nonZeroBalance);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> accountService.deactivateAccount(accountId));
        assertTrue(exception.getMessage().contains("Cannot deactivate account with non-zero balance"));
    }

    @Test
    void deactivateAccount_zeroBalance_deactivatesAccount() {
        // Arrange
        String accountId = "acc-123";
        BigDecimal zeroBalance = BigDecimal.ZERO;

        Account account = Account.builder()
                .id(accountId)
                .code("TEST_CODE")
                .name("Test Account")
                .type(AccountType.ASSET)
                .currency(Currency.KES)
                .isActive(true)
                .build();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));
        when(transactionEntryRepository.getAccountBalanceAsOf(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(zeroBalance);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setIsActive(false);
            return saved;
        });

        // Act
        AccountResponse response = accountService.deactivateAccount(accountId);

        // Assert
        assertNotNull(response);
        verify(accountRepository).save(any(Account.class));
    }

}
