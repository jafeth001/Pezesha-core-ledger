package com.fintech.pezesha_core_ledger.service;

import com.fintech.pezesha_core_ledger.dto.BalanceSheetResponse;
import com.fintech.pezesha_core_ledger.dto.LoanAgingResponse;
import com.fintech.pezesha_core_ledger.dto.TransactionEntryResponse;
import com.fintech.pezesha_core_ledger.dto.TrialBalanceResponse;
import com.fintech.pezesha_core_ledger.enums.AccountType;
import com.fintech.pezesha_core_ledger.enums.LoanStatus;
import com.fintech.pezesha_core_ledger.exception.ResourceNotFoundException;
import com.fintech.pezesha_core_ledger.models.Account;
import com.fintech.pezesha_core_ledger.models.Loan;
import com.fintech.pezesha_core_ledger.models.TransactionEntry;
import com.fintech.pezesha_core_ledger.repository.AccountRepository;
import com.fintech.pezesha_core_ledger.repository.LoanRepository;
import com.fintech.pezesha_core_ledger.repository.TransactionEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService {

    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final TransactionEntryRepository transactionEntryRepository;

    @Cacheable(value = "trialBalance", key = "#asOfDate?.toString() ?: 'current'")
    public TrialBalanceResponse getTrialBalance(LocalDateTime asOfDate) {
        LocalDateTime queryDate = asOfDate != null ? asOfDate : LocalDateTime.now();

        // Fetch all accounts in a single query
        List<Account> accounts = accountRepository.findByIsActiveTrue();

        // Initialize maps
        Map<AccountType, BigDecimal> debitsByType = new EnumMap<>(AccountType.class);
        Map<AccountType, BigDecimal> creditsByType = new EnumMap<>(AccountType.class);
        for (AccountType t : AccountType.values()) {
            debitsByType.put(t, BigDecimal.ZERO);
            creditsByType.put(t, BigDecimal.ZERO);
        }

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        // Calculate balances for each account
        for (Account account : accounts) {
            BigDecimal balance = transactionEntryRepository.getAccountBalanceAsOf(account.getId(), queryDate);

            // Determine if balance is debit or credit based on account type
            BigDecimal debitAmount = BigDecimal.ZERO;
            BigDecimal creditAmount = BigDecimal.ZERO;

            if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                switch (account.getType()) {
                    case ASSET:
                    case EXPENSE:
                        debitAmount = balance;
                        break;
                    case LIABILITY:
                    case EQUITY:
                    case INCOME:
                        creditAmount = balance.abs();
                        break;
                }
            } else {
                switch (account.getType()) {
                    case ASSET:
                    case EXPENSE:
                        creditAmount = balance.abs();
                        break;
                    case LIABILITY:
                    case EQUITY:
                    case INCOME:
                        debitAmount = balance.abs();
                        break;
                }
            }

            // Add to respective totals
            debitsByType.put(account.getType(),
                    debitsByType.get(account.getType()).add(debitAmount));
            creditsByType.put(account.getType(),
                    creditsByType.get(account.getType()).add(creditAmount));

            totalDebits = totalDebits.add(debitAmount);
            totalCredits = totalCredits.add(creditAmount);
        }

        Map<AccountType, TrialBalanceResponse.AccountTypeSummary> summaries = new EnumMap<>(AccountType.class);
        for (AccountType t : AccountType.values()) {
            summaries.put(t, TrialBalanceResponse.AccountTypeSummary.builder()
                    .totalDebits(debitsByType.get(t))
                    .totalCredits(creditsByType.get(t))
                    .build());
        }

        return TrialBalanceResponse.builder()
                .asOfDate(queryDate)
                .accountTypeSummaries(summaries)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .isBalanced(totalDebits.compareTo(totalCredits) == 0)
                .build();
    }

  // Enhanced getBalanceSheet
@Cacheable(value = "balanceSheet", key = "#asOfDate?.toString() ?: 'current'")
public BalanceSheetResponse getBalanceSheet(LocalDateTime asOfDate) {
    LocalDateTime queryDate = asOfDate != null ? asOfDate : LocalDateTime.now();

    List<Account> accounts = accountRepository.findByIsActiveTrue();

    List<BalanceSheetResponse.AccountBalance> assets = new ArrayList<>();
    List<BalanceSheetResponse.AccountBalance> liabilities = new ArrayList<>();
    List<BalanceSheetResponse.AccountBalance> equity = new ArrayList<>();
    List<BalanceSheetResponse.AccountBalance> income = new ArrayList<>();
    List<BalanceSheetResponse.AccountBalance> expenses = new ArrayList<>();

    BigDecimal totalAssets = BigDecimal.ZERO;
    BigDecimal totalLiabilities = BigDecimal.ZERO;
    BigDecimal totalEquity = BigDecimal.ZERO;
    BigDecimal totalIncome = BigDecimal.ZERO;
    BigDecimal totalExpenses = BigDecimal.ZERO;

    for (Account account : accounts) {
        BigDecimal balance = transactionEntryRepository.getAccountBalanceAsOf(account.getId(), queryDate);

        BalanceSheetResponse.AccountBalance ab = BalanceSheetResponse.AccountBalance.builder()
                .accountId(account.getId())
                .accountCode(account.getCode())
                .accountName(account.getName())
                .balance(balance)
                .build();

        switch (account.getType()) {
            case ASSET:
                assets.add(ab);
                totalAssets = totalAssets.add(balance);
                break;
            case LIABILITY:
                liabilities.add(ab);
                totalLiabilities = totalLiabilities.add(balance);
                break;
            case EQUITY:
                equity.add(ab);
                totalEquity = totalEquity.add(balance);
                break;
            case INCOME:
                income.add(ab);
                totalIncome = totalIncome.add(balance);
                break;
            case EXPENSE:
                expenses.add(ab);
                totalExpenses = totalExpenses.add(balance);
                break;
        }
    }

    // Net income/loss affects equity
    BigDecimal netIncome = totalIncome.subtract(totalExpenses);
    totalEquity = totalEquity.add(netIncome);

    BalanceSheetResponse.AccountTypeSummary assetsSummary = BalanceSheetResponse.AccountTypeSummary.builder()
            .accounts(assets)
            .build();

    BalanceSheetResponse.AccountTypeSummary liabilitiesSummary = BalanceSheetResponse.AccountTypeSummary.builder()
            .accounts(liabilities)
            .build();

    BalanceSheetResponse.AccountTypeSummary equitySummary = BalanceSheetResponse.AccountTypeSummary.builder()
            .accounts(equity)
            .build();

    BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);
    boolean isBalanced = totalAssets.compareTo(totalLiabilitiesAndEquity) == 0;

    return BalanceSheetResponse.builder()
            .asOfDate(queryDate)
            .assets(assetsSummary)
            .liabilities(liabilitiesSummary)
            .equity(equitySummary)
            .totalAssets(totalAssets)
            .totalLiabilities(totalLiabilities)
            .totalEquity(totalEquity)
            .isBalanced(isBalanced)
            .build();
}

public Page<TransactionEntryResponse> getTransactionHistory(
        String accountId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable) {

    Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    Page<TransactionEntry> entries = transactionEntryRepository
            .findByAccountIdAndDateRange(accountId, startDate, endDate, pageable);

    return entries.map(this::mapToTransactionEntryResponse);
}

    public LoanAgingResponse getLoanAgingReport() {
        List<Loan> loans = loanRepository.findByStatusIn(Arrays.asList(LoanStatus.ACTIVE, LoanStatus.DISBURSED));
        LocalDateTime currentDate = LocalDateTime.now();

        Map<String, LoanAgingResponse.LoanAgingBucket> buckets = new LinkedHashMap<>();
        buckets.put("CURRENT", new LoanAgingResponse.LoanAgingBucket(0L, BigDecimal.ZERO));
        buckets.put("30-59_DAYS", new LoanAgingResponse.LoanAgingBucket(0L, BigDecimal.ZERO));
        buckets.put("60-89_DAYS", new LoanAgingResponse.LoanAgingBucket(0L, BigDecimal.ZERO));
        buckets.put("90_PLUS_DAYS", new LoanAgingResponse.LoanAgingBucket(0L, BigDecimal.ZERO));

        for (Loan loan : loans) {
            if (loan.getDueDate() == null) continue;

            long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate().toLocalDate(), currentDate.toLocalDate());
            String bucketKey;

            if (daysOverdue <= 29) {
                bucketKey = "CURRENT";
            } else if (daysOverdue <= 59) {
                bucketKey = "30-59_DAYS";
            } else if (daysOverdue <= 89) {
                bucketKey = "60-89_DAYS";
            } else {
                bucketKey = "90_PLUS_DAYS";
            }

            LoanAgingResponse.LoanAgingBucket bucket = buckets.get(bucketKey);
            bucket.setCount(bucket.getCount() + 1);
            bucket.setTotalAmount(bucket.getTotalAmount().add(loan.getOutstandingBalance()));
        }

        return LoanAgingResponse.builder()
                .buckets(buckets)
                .build();
    }

private TransactionEntryResponse mapToTransactionEntryResponse(TransactionEntry entry) {
    return TransactionEntryResponse.builder()
            .accountId(entry.getAccount().getId())
            .accountCode(entry.getAccount().getCode())
            .debit(entry.getDebit())
            .credit(entry.getCredit())
            .currency(entry.getCurrency())
            .runningBalance(entry.getRunningBalance())
            .build();
}

}
