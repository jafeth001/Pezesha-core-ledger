package com.fintech.pezesha_core_ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.pezesha_core_ledger.dto.AccountBalanceResponse;
import com.fintech.pezesha_core_ledger.dto.AccountResponse;
import com.fintech.pezesha_core_ledger.dto.CreateAccountRequest;
import com.fintech.pezesha_core_ledger.service.AccountService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@WithMockUser(username = "test-user", roles = {"USER"})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_returnsCreated() throws Exception {

        CreateAccountRequest req = CreateAccountRequest.builder()
                .code("CASH_MONEY")
                .name("Cash Account")
                .type(com.fintech.pezesha_core_ledger.enums.AccountType.ASSET)
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .build();

        AccountResponse response = AccountResponse.builder()
                .id("acc-123")
                .code("CASH_MONEY")
                .name("Cash Account")
                .type(com.fintech.pezesha_core_ledger.enums.AccountType.ASSET)
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .isActive(true)
                .build();

        when(accountService.createAccount(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("acc-123"))
                .andExpect(jsonPath("$.code").value("CASH_MONEY"))
                .andExpect(jsonPath("$.name").value("Cash Account"));
    }

    @Test
    void getAccount_returnsOk() throws Exception {

        AccountResponse response = AccountResponse.builder()
                .id("acc-123")
                .code("CASH_MONEY")
                .name("Cash Account")
                .type(com.fintech.pezesha_core_ledger.enums.AccountType.ASSET)
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .isActive(true)
                .build();

        when(accountService.getAccount("acc-123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/acc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("acc-123"))
                .andExpect(jsonPath("$.code").value("CASH_MONEY"));
    }

    @Test
    void getAccountBalance_returnsOk() throws Exception {

        AccountBalanceResponse response = AccountBalanceResponse.builder()
                .accountId("acc-123")
                .accountCode("CASH_MONEY")
                .accountName("Cash Account")
                .currency(com.fintech.pezesha_core_ledger.enums.Currency.KES)
                .balance(new BigDecimal("5000"))
                .asOfDate(LocalDateTime.now())
                .build();

        when(accountService.getAccountBalance("acc-123", null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/acc-123/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("acc-123"))
                .andExpect(jsonPath("$.balance").value(5000));
    }

    @Test
    void getAllAccounts_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk());
    }

    @Test
    void getAccountsByType_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/type/ASSET"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateAccount_returnsOk() throws Exception {

        AccountResponse response = AccountResponse.builder()
                .id("acc-123")
                .code("CASH_MONEY")
                .name("Cash Account")
                .isActive(false)
                .build();

        when(accountService.deactivateAccount("acc-123")).thenReturn(response);

        mockMvc.perform(delete("/api/v1/accounts/acc-123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("acc-123"))
                .andExpect(jsonPath("$.isActive").value(false));
    }
}
