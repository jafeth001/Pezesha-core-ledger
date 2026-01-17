package com.fintech.pezesha_core_ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.pezesha_core_ledger.dto.BalanceSheetResponse;
import com.fintech.pezesha_core_ledger.dto.LoanAgingResponse;
import com.fintech.pezesha_core_ledger.dto.TrialBalanceResponse;
import com.fintech.pezesha_core_ledger.service.ReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportingController.class)
@WithMockUser(username = "test-user", roles = {"USER"})
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportingService reportingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTrialBalance_returnsOk() throws Exception {

        TrialBalanceResponse response = TrialBalanceResponse.builder()
                .asOfDate(LocalDateTime.now())
                .totalDebits(new BigDecimal("10000"))
                .totalCredits(new BigDecimal("10000"))
                .isBalanced(true)
                .build();

        when(reportingService.getTrialBalance(null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/trial-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDebits").value(10000))
                .andExpect(jsonPath("$.totalCredits").value(10000))
                .andExpect(jsonPath("$.isBalanced").value(true));
    }

    @Test
    void getBalanceSheet_returnsOk() throws Exception {

        BalanceSheetResponse response = BalanceSheetResponse.builder()
                .asOfDate(LocalDateTime.now())
                .totalAssets(new BigDecimal("15000"))
                .totalLiabilities(new BigDecimal("5000"))
                .totalEquity(new BigDecimal("10000"))
                .isBalanced(true)
                .build();

        when(reportingService.getBalanceSheet(null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/balance-sheet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssets").value(15000))
                .andExpect(jsonPath("$.totalLiabilities").value(5000));
    }

    @Test
    void getLoanAgingReport_returnsOk() throws Exception {

        Map<String, LoanAgingResponse.LoanAgingBucket> buckets = new HashMap<>();
        buckets.put("CURRENT", new LoanAgingResponse.LoanAgingBucket(5L, new BigDecimal("5000")));
        buckets.put("30-59_DAYS", new LoanAgingResponse.LoanAgingBucket(2L, new BigDecimal("2000")));

        LoanAgingResponse response = LoanAgingResponse.builder()
                .buckets(buckets)
                .build();

        when(reportingService.getLoanAgingReport()).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/loan-aging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buckets.CURRENT.count").value(5))
                .andExpect(jsonPath("$.buckets.CURRENT.totalAmount").value(5000))
                .andExpect(jsonPath("$.buckets['30-59_DAYS'].count").value(2));
    }

    @Test
    void getTransactionHistory_returnsOk() throws Exception {

        mockMvc.perform(get("/api/v1/reports/account/acc-123/history"))
                .andExpect(status().isOk());
    }
}
