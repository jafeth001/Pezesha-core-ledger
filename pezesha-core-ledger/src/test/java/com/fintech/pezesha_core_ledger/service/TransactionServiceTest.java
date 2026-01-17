package com.fintech.pezesha_core_ledger.service;

import com.fintech.pezesha_core_ledger.dto.*;
import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.enums.TransactionStatus;
import com.fintech.pezesha_core_ledger.exception.AccountingException;
import com.fintech.pezesha_core_ledger.exception.ResourceNotFoundException;
import com.fintech.pezesha_core_ledger.models.Account;
import com.fintech.pezesha_core_ledger.models.Transaction;
import com.fintech.pezesha_core_ledger.models.TransactionEntry;
import com.fintech.pezesha_core_ledger.repository.AccountRepository;
import com.fintech.pezesha_core_ledger.repository.TransactionRepository;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private IdempotencyService idempotencyService;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository,
                accountRepository, idempotencyService);
    }

    @Test
    void postTransaction_validRequest_createsTransaction() {
        // Arrange
        String idempotencyKey = "test-key";
        TransactionRequest request = TransactionRequest.builder()
                .idempotencyKey(idempotencyKey)
                .description("Test transaction")
                .entries(Arrays.asList(
                        TransactionEntryRequest.builder()
                                .accountId("acc-123")
                                .debit(new BigDecimal("100"))
                                .credit(new BigDecimal("0"))
                                .currency(Currency.KES)
                                .build(),
                        TransactionEntryRequest.builder()
                                .accountId("acc-456")
                                .debit(new BigDecimal("0"))
                                .credit(new BigDecimal("100"))
                                .currency(Currency.KES)
                                .build()
                ))
                .build();

        Account account1 = Account.builder()
                .id("acc-123")
                .code("ACC123")
                .currency(Currency.KES)
                .isActive(true)
                .build();

        Account account2 = Account.builder()
                .id("acc-456")
                .code("ACC456")
                .currency(Currency.KES)
                .isActive(true)
                .build();

        when(transactionRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(accountRepository.findById("acc-123")).thenReturn(Optional.of(account1));
        when(accountRepository.findById("acc-456")).thenReturn(Optional.of(account2));
        when(accountRepository.getReferenceById("acc-123")).thenReturn(account1);
        when(accountRepository.getReferenceById("acc-456")).thenReturn(account2);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId("trans-123");
            saved.setPostedAt(LocalDateTime.now());
            return saved;
        });

        // Act
        TransactionResponse response = transactionService.postTransaction(request);

        // Assert
        assertNotNull(response);
        assertEquals(idempotencyKey, response.getIdempotencyKey());
        assertEquals("POSTED", response.getStatus());
        assertEquals(2, response.getEntries().size());

        verify(transactionRepository).save(any(Transaction.class));
        verify(idempotencyService).storeIdempotencyKey(eq(idempotencyKey), any(TransactionResponse.class));
    }

    @Test
    void postTransaction_unbalancedTransaction_throwsAccountingException() {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .idempotencyKey("test-key")
                .description("Unbalanced transaction")
                .entries(Arrays.asList(
                        TransactionEntryRequest.builder()
                                .accountId("acc-123")
                                .debit(new BigDecimal("100"))
                                .credit(new BigDecimal("0"))
                                .currency(Currency.KES)
                                .build(),
                        TransactionEntryRequest.builder()
                                .accountId("acc-456")
                                .debit(new BigDecimal("0"))
                                .credit(new BigDecimal("50"))
                                .currency(Currency.KES)
                                .build()
                ))
                .build();

        // Act & Assert
        AccountingException exception = assertThrows(AccountingException.class,
                () -> transactionService.postTransaction(request));
        assertTrue(exception.getMessage().contains("Transaction unbalanced"));
    }

    @Test
    void reverseTransaction_validTransaction_reversesSuccessfully() {
        // Arrange
        String transactionId = "trans-123";
        String reason = "Incorrect transaction";

        Transaction originalTransaction = Transaction.builder()
                .id(transactionId)
                .idempotencyKey("orig-key")
                .description("Original transaction")
                .status(TransactionStatus.POSTED)
                .postedAt(LocalDateTime.now())
                .build();

        Account account1 = Account.builder()
                .id("acc-123")
                .code("ACC123")
                .currency(Currency.KES)
                .isActive(true)
                .build();

        TransactionEntry entry1 = TransactionEntry.builder()
                .account(account1)
                .debit(new BigDecimal("100"))
                .credit(new BigDecimal("0"))
                .currency(Currency.KES)
                .build();

        Account account2 = Account.builder()
                .id("acc-456")
                .code("ACC456")
                .currency(Currency.KES)
                .isActive(true)
                .build();

        TransactionEntry entry2 = TransactionEntry.builder()
                .account(account2)
                .debit(new BigDecimal("0"))
                .credit(new BigDecimal("100"))
                .currency(Currency.KES)
                .build();

        originalTransaction.addEntry(entry1);
        originalTransaction.addEntry(entry2);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(originalTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.findById("acc-123")).thenReturn(Optional.of(account1));
        when(accountRepository.findById("acc-456")).thenReturn(Optional.of(account2));
        when(accountRepository.getReferenceById("acc-123")).thenReturn(account1);
        when(accountRepository.getReferenceById("acc-456")).thenReturn(account2);

        // Act
        TransactionResponse response = transactionService.reverseTransaction(transactionId, reason);

        // Assert
        assertNotNull(response);
        assertTrue(response.getDescription().contains("Reversal: Original transaction"));
        assertTrue(response.getDescription().contains("Reason: " + reason));

        verify(transactionRepository).save(argThat(trans -> trans.getStatus() == TransactionStatus.REVERSED));
    }

    @Test
    void getTransaction_transactionExists_returnsTransaction() {
        // Arrange
        String transactionId = "trans-123";
        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .idempotencyKey("test-key")
                .description("Test transaction")
                .status(TransactionStatus.POSTED)
                .postedAt(LocalDateTime.now())
                .build();

        transaction.addEntry(TransactionEntry.builder()
                .account(Account.builder()
                        .id("dummy-acc")
                        .code("DUMMY")
                        .currency(Currency.KES)
                        .isActive(true)
                        .build())
                .debit(BigDecimal.ZERO)
                .credit(BigDecimal.ZERO)
                .currency(Currency.KES)
                .build());

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        // Act
        TransactionResponse response = transactionService.getTransaction(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(transactionId, response.getId());
    }

    @Test
    void getTransaction_transactionNotExists_throwsResourceNotFoundException() {
        // Arrange
        String transactionId = "non-existent";

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransaction(transactionId));
    }
}
