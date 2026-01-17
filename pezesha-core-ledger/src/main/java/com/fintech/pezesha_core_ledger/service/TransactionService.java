package com.fintech.pezesha_core_ledger.service;

import com.fintech.pezesha_core_ledger.dto.TransactionEntryRequest;
import com.fintech.pezesha_core_ledger.dto.TransactionEntryResponse;
import com.fintech.pezesha_core_ledger.dto.TransactionRequest;
import com.fintech.pezesha_core_ledger.dto.TransactionResponse;
import com.fintech.pezesha_core_ledger.enums.TransactionStatus;
import com.fintech.pezesha_core_ledger.exception.AccountingException;
import com.fintech.pezesha_core_ledger.exception.ConcurrencyException;
import com.fintech.pezesha_core_ledger.exception.ResourceNotFoundException;
import com.fintech.pezesha_core_ledger.exception.ValidationException;
import com.fintech.pezesha_core_ledger.models.Account;
import com.fintech.pezesha_core_ledger.models.Transaction;
import com.fintech.pezesha_core_ledger.models.TransactionEntry;
import com.fintech.pezesha_core_ledger.repository.AccountRepository;
import com.fintech.pezesha_core_ledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final Map<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();
    private final IdempotencyService idempotencyService;

    @Transactional
    @CacheEvict(value = {"accountBalance", "trialBalance", "balanceSheet"}, allEntries = true)
    public TransactionResponse postTransaction(TransactionRequest request) {
        log.info("Processing transaction with idempotency key: {}", request.getIdempotencyKey());

        // Idempotency check
        String idempotencyKey = request.getIdempotencyKey();

        // Check idempotency cache first
        if (idempotencyService.isDuplicate(idempotencyKey)) {
            TransactionResponse cached = idempotencyService.getIdempotentResult(idempotencyKey, TransactionResponse.class);
            if (cached != null) {
                log.info("Returning cached transaction for idempotency key: {}", idempotencyKey);
                return cached;
            }
        }

        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            TransactionResponse resp = mapToResponse(existingTransaction.get());
            // store into idempotency cache for faster subsequent lookups
            try {
                idempotencyService.storeIdempotencyKey(idempotencyKey, resp);
            } catch (Exception e) {
                log.warn("Failed to store idempotency key in cache: {}", e.getMessage());
            }
            log.info("Returning existing transaction for idempotency key: {}", idempotencyKey);
            return resp;
        }

        // Validate transaction
        validateTransaction(request);

        // Get all affected account IDs
        Set<String> accountIds = request.getEntries().stream()
                .map(TransactionEntryRequest::getAccountId)
                .collect(Collectors.toSet());

        // Acquire local locks for all affected accounts (ordered to avoid deadlocks)
        List<ReentrantLock> locks = acquireAccountLocks(accountIds);
        try {
            // Create and save transaction while holding locks
            Transaction transaction = createTransaction(request);
            Transaction savedTransaction = transactionRepository.save(transaction);

            log.info("Transaction posted successfully: {}", savedTransaction.getId());
            TransactionResponse response = mapToResponse(savedTransaction);

            try {
                idempotencyService.storeIdempotencyKey(idempotencyKey, response);
            } catch (Exception e) {
                log.warn("Failed to store idempotency key in cache: {}", e.getMessage());
            }

            return response;

        } finally {
            // Release all locks
            releaseLocks(locks);
        }
    }

    private void validateTransaction(TransactionRequest request) {
        // Validate debits equal credits
        BigDecimal totalDebits = request.getEntries().stream()
                .map(TransactionEntryRequest::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = request.getEntries().stream()
                .map(TransactionEntryRequest::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new AccountingException(
                    String.format("Transaction unbalanced: Debits %s != Credits %s", totalDebits, totalCredits)
            );
        }

        // Validate each entry
        for (TransactionEntryRequest entry : request.getEntries()) {
            validateTransactionEntry(entry);
        }
    }

    private void validateTransactionEntry(TransactionEntryRequest entry) {
        // Validate debit/credit rules
        boolean hasDebit = entry.getDebit().compareTo(BigDecimal.ZERO) > 0;
        boolean hasCredit = entry.getCredit().compareTo(BigDecimal.ZERO) > 0;

        if (hasDebit && hasCredit) {
            throw new ValidationException("Entry cannot have both debit and credit amounts");
        }

        if (!hasDebit && !hasCredit) {
            throw new ValidationException("Entry must have either debit or credit amount");
        }

        // Validate account exists and is active
        Account account = accountRepository.findById(entry.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + entry.getAccountId()));

        if (!account.getIsActive()) {
            throw new ValidationException("Account is inactive: " + entry.getAccountId());
        }

        // Validate currency matches account currency
        if (!account.getCurrency().equals(entry.getCurrency())) {
            throw new ValidationException(
                    String.format("Currency mismatch for account %s. Expected: %s, Got: %s",
                            account.getCode(), account.getCurrency(), entry.getCurrency())
            );
        }
    }

    private Transaction createTransaction(TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .description(request.getDescription())
                .status(TransactionStatus.POSTED)
                .postedAt(LocalDateTime.now())
                .build();

        // Create transaction entries
        for (TransactionEntryRequest entryRequest : request.getEntries()) {
            Account account = accountRepository.getReferenceById(entryRequest.getAccountId());

            TransactionEntry entry = TransactionEntry.builder()
                    .account(account)
                    .debit(entryRequest.getDebit())
                    .credit(entryRequest.getCredit())
                    .currency(entryRequest.getCurrency())
                    .postedAt(transaction.getPostedAt())
                    .build();

            transaction.addEntry(entry);
        }

        return transaction;
    }

    private List<ReentrantLock> acquireAccountLocks(Set<String> accountIds) {
        List<ReentrantLock> acquired = new ArrayList<>();

        // Acquire locks in deterministic order to avoid deadlocks
        List<String> sorted = new ArrayList<>(accountIds);
        Collections.sort(sorted);

        for (String accountId : sorted) {
            // if lock already exists re-use it
            ReentrantLock lock = accountLocks.computeIfAbsent(accountId, k -> new ReentrantLock());
            boolean locked = false;
            try {
                // create a new lock  if not
                locked = lock.tryLock(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (!locked) {
                // release already acquired locks
                releaseLocks(acquired);
                throw new ConcurrencyException("Failed to acquire lock for account: " + accountId);
            }

            acquired.add(lock);
            log.debug("Acquired local lock for account: {}", accountId);
        }

        return acquired;
    }

    private void releaseLocks(List<ReentrantLock> locks) {
        for (ReentrantLock lock : locks) {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.debug("Released local lock");
                }
            } catch (Exception e) {
                log.warn("Error releasing local lock", e);
            }
        }
    }

    @Transactional
    @CacheEvict(value = {"accountBalance", "trialBalance", "balanceSheet"}, allEntries = true)
    public TransactionResponse reverseTransaction(String transactionId, String reason) {
        log.info("Reversing transaction: {}", transactionId);

        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (original.getStatus() == TransactionStatus.REVERSED) {
            throw new ValidationException("Transaction already reversed");
        }

        // Create reversal transaction
        TransactionRequest reversalRequest = createReversalRequest(original, reason);
        TransactionResponse reversal = postTransaction(reversalRequest);

        // Update original transaction status
        original.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(original);

        log.info("Transaction reversed successfully: {}", transactionId);
        return reversal;
    }

    private TransactionRequest createReversalRequest(Transaction original, String reason) {
        List<TransactionEntryRequest> reversalEntries = original.getEntries().stream()
                .map(entry -> TransactionEntryRequest.builder()
                        .accountId(entry.getAccount().getId())
                        .debit(entry.getCredit())  // Swap debits and credits
                        .credit(entry.getDebit())
                        .currency(entry.getCurrency())
                        .build())
                .collect(Collectors.toList());

        return TransactionRequest.builder()
                .idempotencyKey("reversal_" + original.getId() + "_" + System.currentTimeMillis())
                .description("Reversal: " + original.getDescription() + " | Reason: " + reason)
                .entries(reversalEntries)
                .build();
    }

    public TransactionResponse getTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        List<TransactionEntryResponse> entryResponses = transaction.getEntries().stream()
                .map(entry -> TransactionEntryResponse.builder()
                        .accountId(entry.getAccount().getId())
                        .accountCode(entry.getAccount().getCode())
                        .debit(entry.getDebit())
                        .credit(entry.getCredit())
                        .currency(entry.getCurrency())
                        .runningBalance(entry.getRunningBalance())
                        .build())
                .collect(Collectors.toList());

        return TransactionResponse.builder()
                .id(transaction.getId())
                .idempotencyKey(transaction.getIdempotencyKey())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .postedAt(transaction.getPostedAt())
                .entries(entryResponses)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
