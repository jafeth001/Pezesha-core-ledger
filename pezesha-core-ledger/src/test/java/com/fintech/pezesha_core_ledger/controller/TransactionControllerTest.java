package com.fintech.pezesha_core_ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.pezesha_core_ledger.dto.*;
import com.fintech.pezesha_core_ledger.enums.Currency;
import com.fintech.pezesha_core_ledger.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
@WithMockUser(username = "test-user", roles = {"USER"})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void postTransaction_returnsCreated() throws Exception {

        TransactionRequest req = TransactionRequest.builder()
                .idempotencyKey("trans-key-1")
                .description("Test transaction")
                .entries(List.of(
                        TransactionEntryRequest.builder()
                                .accountId("acc-123")
                                .debit(new BigDecimal("1000"))
                                .credit(BigDecimal.ZERO)
                                .currency(Currency.KES)
                                .build(),
                        TransactionEntryRequest.builder()
                                .accountId("acc-456")
                                .debit(BigDecimal.ZERO)
                                .credit(new BigDecimal("1000"))
                                .currency(Currency.KES)
                                .build()
                ))
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id("trans-123")
                .idempotencyKey("trans-key-1")
                .description("Test transaction")
                .status("POSTED")
                .postedAt(LocalDateTime.now())
                .build();

        when(transactionService.postTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idempotencyKey").value("trans-key-1"))
                .andExpect(jsonPath("$.status").value("POSTED"));
    }

    @Test
    void getTransaction_returnsOk() throws Exception {

        TransactionResponse response = TransactionResponse.builder()
                .id("trans-123")
                .status("POSTED")
                .build();

        when(transactionService.getTransaction("trans-123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/trans-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("trans-123"));
    }

    @Test
    void getAllTransactions_returnsOk() throws Exception {

        when(transactionService.getAllTransactions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    void reverseTransaction_returnsOk() throws Exception {

        ReverseTransactionRequest req = ReverseTransactionRequest.builder()
                .reason("Incorrect transaction")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id("reverse-trans-123")
                .status("POSTED")
                .build();

        when(transactionService.reverseTransaction(eq("trans-123"), eq("Incorrect transaction")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions/trans-123/reverse")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("reverse-trans-123"));
    }
}
