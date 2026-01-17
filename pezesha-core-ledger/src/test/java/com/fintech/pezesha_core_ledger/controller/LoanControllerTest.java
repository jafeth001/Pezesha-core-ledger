package com.fintech.pezesha_core_ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.pezesha_core_ledger.dto.*;
import com.fintech.pezesha_core_ledger.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@WithMockUser(username = "test-user", roles = {"USER"})
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void apply_returnsOk() throws Exception {

        LoanApplicationRequest req = LoanApplicationRequest.builder()
                .accountId("acc-123")
                .principalAmount(new BigDecimal("10000"))
                .interestRate(new BigDecimal("10.5"))
                .termInDays(365)
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .dueDate(LocalDateTime.now().plusDays(365))
                .build();

        LoanResponse response = LoanResponse.builder()
                .loanId("loan-123")
                .accountId("acc-123")
                .principalAmount(new BigDecimal("10000"))
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .status(com.fintech.pezesha_core_ledger.enums.LoanStatus.PENDING)
                .build();

        when(loanService.applyForLoan(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/loans")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value("loan-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void disburse_returnsOk() throws Exception {

        DisbursementRequest req = DisbursementRequest.builder()
                .idempotencyKey("disburse-key-1")
                .amount(new BigDecimal("10000"))
                .originationFee(new BigDecimal("500"))
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .loansReceivableAccountId("acc-loan-recv")
                .cashAccountId("acc-cash")
                .origFeeReceivableAccountId("acc-orig-recv")
                .feeIncomeAccountId("acc-fee-income")
                .build();

        LoanResponse response = LoanResponse.builder()
                .loanId("loan-123")
                .status(com.fintech.pezesha_core_ledger.enums.LoanStatus.DISBURSED)
                .disbursementDate(LocalDateTime.now())
                .build();

        when(loanService.disburseLoan(eq("loan-123"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/loans/loan-123/disburse")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISBURSED"));
    }

    @Test
    void repay_returnsOk() throws Exception {

        RepaymentRequest req = RepaymentRequest.builder()
                .idempotencyKey("repay-key-1")
                .amount(new BigDecimal("1200"))
                .principalPortion(new BigDecimal("1000"))
                .interestPortion(new BigDecimal("200"))
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .cashAccountId("acc-cash")
                .loansReceivableAccountId("acc-loan-recv")
                .interestIncomeAccountId("acc-int-income")
                .build();

        LoanResponse response = LoanResponse.builder()
                .loanId("loan-123")
                .outstandingBalance(new BigDecimal("8800"))
                .build();

        when(loanService.repayLoan(eq("loan-123"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/loans/loan-123/repay")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outstandingBalance").value(8800));
    }

    @Test
    void writeOff_returnsOk() throws Exception {

        WriteOffRequest req = WriteOffRequest.builder()
                .idempotencyKey("writeoff-key-1")
                .amount(new BigDecimal("5000"))
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .badDebtExpenseAccountId("acc-bad-debt")
                .loansReceivableAccountId("acc-loan-recv")
                .build();

        LoanResponse response = LoanResponse.builder()
                .loanId("loan-123")
                .status(com.fintech.pezesha_core_ledger.enums.LoanStatus.WRITTEN_OFF)
                .build();

        when(loanService.writeOffLoan(eq("loan-123"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/loans/loan-123/write-off")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WRITTEN_OFF"));
    }

    @Test
    void getAllLoans_returnsOk() throws Exception {

        LoanResponse loan = LoanResponse.builder()
                .loanId("loan-123")
                .accountId("acc-123")
                .status(com.fintech.pezesha_core_ledger.enums.LoanStatus.ACTIVE)
                .build();

        when(loanService.getAllLoans()).thenReturn(List.of(loan));

        mockMvc.perform(get("/api/v1/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanId").value("loan-123"));
    }

    @Test
    void approveLoan_returnsOk() throws Exception {

        LoanResponse response = LoanResponse.builder()
                .loanId("loan-123")
                .status(com.fintech.pezesha_core_ledger.enums.LoanStatus.APPROVED)
                .build();

        when(loanService.approveLoan("loan-123")).thenReturn(response);

        mockMvc.perform(put("/api/v1/loans/loan-123/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
