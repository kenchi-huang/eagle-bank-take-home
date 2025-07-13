package com.eaglebank.api.controller;

import com.eaglebank.api.dto.Transaction.CreateTransactionRequest;
import com.eaglebank.api.dto.Transaction.ListTransactionsResponse;
import com.eaglebank.api.dto.Transaction.TransactionResponse;
import com.eaglebank.api.exception.InsufficientFundsException;
import com.eaglebank.api.mapper.TransactionMapper;
import com.eaglebank.api.model.Transaction.Transaction;
import com.eaglebank.api.model.Transaction.TransactionType;
import com.eaglebank.api.security.JwtService;
import com.eaglebank.api.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // FIX: Use the correct test annotation
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
    void createTransaction_shouldReturn201_forValidDeposit() throws Exception {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setType("deposit");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("GBP");
        request.setReference("Salary");

        var createdTransaction = new Transaction();
        createdTransaction.setId("txn-123");
        createdTransaction.setType(TransactionType.DEPOSIT);

        var responseDto = TransactionResponse.builder()
                .id("txn-123")
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .reference("Salary")
                .build();

        when(transactionService.createTransaction(eq("12345"), any(), any())).thenReturn(createdTransaction);
        when(transactionMapper.toResponse(createdTransaction)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/v1/accounts/12345/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("txn-123"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.reference").value("Salary"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTransactions_shouldReturn200_andWrappedListOfTransactions() throws Exception {
        // Arrange
        var transaction = new Transaction();
        transaction.setId("txn-abc");
        var responseDto = TransactionResponse.builder().id("txn-abc").build();
        // The mapper returns a List, so we mock it to return a List.
        var responseDtoList = ListTransactionsResponse.builder().transactions(List.of(responseDto)).build();

        when(transactionService.getTransactionsForAccount(eq("12345"), any())).thenReturn(List.of(transaction));
        // FIX: The mock should return what the real mapper returns (a List)
        when(transactionMapper.toResponseList(List.of(transaction))).thenReturn(responseDtoList);

        // Act & Assert
        mockMvc.perform(get("/v1/accounts/12345/transactions"))
                .andExpect(status().isOk())
                // Assert the wrapper object structure created by the controller
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].id").value("txn-abc"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createTransaction_shouldReturn422_whenFundsAreInsufficient() throws Exception {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setType("withdrawal");
        request.setAmount(new BigDecimal("5000.00"));
        request.setCurrency("GBP");

        // Mock the service to throw the specific exception
        String errorMessage = "Insufficient funds for this withdrawal.";
        when(transactionService.createTransaction(eq("12345"), any(), any()))
                .thenThrow(new InsufficientFundsException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/v1/accounts/12345/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}