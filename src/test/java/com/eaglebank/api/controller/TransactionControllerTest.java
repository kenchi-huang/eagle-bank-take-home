package com.eaglebank.api.controller;

import com.eaglebank.api.dto.Transaction.CreateTransactionRequest;
import com.eaglebank.api.dto.Transaction.TransactionResponse;
import com.eaglebank.api.mapper.TransactionMapper;
import com.eaglebank.api.model.Transaction.Transaction;
import com.eaglebank.api.security.JwtService;
import com.eaglebank.api.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private TransactionMapper transactionMapper;

    // Required mocks for security context
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "test@example.com")
    void createTransaction_shouldReturn201AndTransactionResponse() throws Exception {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setToAccountNumber("98765");
        request.setAmount(new BigDecimal("50.00"));

        var createdTransaction = new Transaction();
        createdTransaction.setId("tx-123");
        var responseDto = TransactionResponse.builder().id("tx-123").build();

        when(transactionService.createTransaction(eq("12345"), any(), any())).thenReturn(createdTransaction);
        when(transactionMapper.toResponse(createdTransaction)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/v1/accounts/12345/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("tx-123"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTransactions_shouldReturn200AndListOfTransactions() throws Exception {
        // Arrange
        var transaction = new Transaction();
        transaction.setId("tx-abc");
        var responseDto = TransactionResponse.builder().id("tx-abc").build();

        when(transactionService.getTransactionsForAccount(eq("12345"), any())).thenReturn(List.of(transaction));
        when(transactionMapper.toResponseList(List.of(transaction))).thenReturn(List.of(responseDto));

        // Act & Assert
        mockMvc.perform(get("/v1/accounts/12345/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("tx-abc"));
    }
}
