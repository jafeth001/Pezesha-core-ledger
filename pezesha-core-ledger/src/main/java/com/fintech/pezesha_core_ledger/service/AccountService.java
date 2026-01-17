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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionEntryRepository transactionEntryRepository;

    /* CREATE ACCOUNT */

    @Transactional
    @CacheEvict(
            value = {"accountById", "allAccounts"},
            allEntries = true
    )
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account with code: {}", request.getCode());

        if (accountRepository.findByCode(request.getCode()).isPresent()) {
            throw new ValidationException("Account code already exists: " + request.getCode());
        }

        Account parent = null;
        if (request.getParentId() != null) {
            parent = accountRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent account not found"));

            if (!parent.getType().equals(request.getType())) {
                throw new ValidationException("Child account must have same type as parent");
            }
        }

        Account account = Account.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .currency(request.getCurrency())
                .parent(parent)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully: {}", savedAccount.getId());

        return mapToResponse(savedAccount);
    }

    /* ACCOUNT BALANCE */
    @Cacheable(
            value = "accountBalance",
            key = "#accountId + '_' + (#asOfDate != null ? #asOfDate.toString() : 'current')",
            unless = "#result.balance == null"
    )
    public AccountBalanceResponse getAccountBalance(String accountId, LocalDateTime asOfDate) {
        log.debug("Getting balance for account: {} as of: {}", accountId, asOfDate);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        LocalDateTime queryDate = asOfDate != null ? asOfDate : LocalDateTime.now();
        BigDecimal balance = transactionEntryRepository.getAccountBalanceAsOf(accountId, queryDate);

        return AccountBalanceResponse.builder()
                .accountId(accountId)
                .accountCode(account.getCode())
                .accountName(account.getName())
                .currency(account.getCurrency())
                .balance(balance)
                .asOfDate(queryDate)
                .build();
    }


    /* GET ACCOUNT */

    @Cacheable(value = "accountById", key = "#accountId")
    public AccountResponse getAccount(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return mapToResponse(account);
    }

    /* GET ALL ACCOUNTS */

    @Cacheable(value = "allAccounts", key = "'ALL'")
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* GET BY TYPE */

    @Cacheable(value = "accountsByType", key = "#type")
    public List<AccountResponse> getAccountsByType(AccountType type) {
        return accountRepository.findByTypeAndCurrency(type, Currency.KES).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* DEACTIVATE */

    @Transactional
    @CacheEvict(
            value = {"accountById", "allAccounts", "accountBalance"},
            allEntries = true
    )
    public AccountResponse deactivateAccount(String accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        BigDecimal balance = transactionEntryRepository
                .getAccountBalanceAsOf(accountId, LocalDateTime.now());

        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Cannot deactivate account with non-zero balance");
        }

        account.setIsActive(false);
        Account updatedAccount = accountRepository.save(account);

        log.info("Account deactivated: {}", accountId);
        return mapToResponse(updatedAccount);
    }

    /**
     * Mapper
     **/
    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .type(account.getType())
                .currency(account.getCurrency())
                .parentId(account.getParent() != null ? account.getParent().getId() : null)
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
