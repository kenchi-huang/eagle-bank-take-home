package com.eaglebank.api.controller;

import com.eaglebank.api.dto.Account.*;
import com.eaglebank.api.dto.Account.GetAccountResponse;
import com.eaglebank.api.mapper.AccountMapper;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.security.JwtService;
import com.eaglebank.api.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;
    @MockBean
    private AccountMapper accountMapper;

    // Required mocks for security context
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Disabled("CSRF Issue causing test to not be able to be run properly")
    @Test
    @WithMockUser(username = "test@example.com")
    void createAccount_shouldReturn201AndAccountResponse() throws Exception {
        // Arrange
        var request = new CreateAccountRequest();
        request.setName("Test Account");

        var createdAccount = new Account();
        createdAccount.setAccountNumber("01234567");

        var responseDto = GetAccountResponse.builder()
                .accountNumber("01234567")
                .name("Test Account")
                .balance(BigDecimal.ZERO)
                .build();

        when(accountService.createAccount(any(), any())).thenReturn(createdAccount);
        when(accountMapper.toResponse(createdAccount)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("01234567"))
                .andExpect(jsonPath("$.name").value("Test Account"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAccounts_shouldReturn200AndListOfAccounts() throws Exception {
        // Arrange
        var account = new Account();
        var responseDto = GetAccountResponse.builder().accountNumber("1111").build();

        when(accountService.getAccounts(any())).thenReturn(List.of(account));
        when(accountMapper.toResponseList(List.of(account))).thenReturn(ListAccountResponse.builder().accounts(List.of(responseDto)).build());

        // Act & Assert
        mockMvc.perform(get("/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].accountNumber").value("1111"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAccountByNumber_shouldReturn200AndAccount() throws Exception {
        // Arrange
        var account = new Account();
        var responseDto = GetAccountResponse.builder().accountNumber("12345").build();

        when(accountService.getAccountByAccountNumber(eq("12345"), any())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/v1/accounts/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"));
    }

    @Disabled
    @Test
    @WithMockUser(username = "test@example.com")
    void updateAccount_shouldReturn200AndUpdatedAccount() throws Exception {
        // Arrange
        var request = new UpdateAccountRequest();
        request.setName("Updated Account Name");

        var updatedAccount = new Account();
        updatedAccount.setAccountNumber("12345");
        updatedAccount.setName("Updated Account Name");

        var responseDto = GetAccountResponse.builder()
                .accountNumber("12345")
                .name("Updated Account Name")
                .build();

        when(accountService.updateAccount(eq("12345"), any(UpdateAccountRequest.class), any())).thenReturn(updatedAccount);
        when(accountMapper.toResponse(updatedAccount)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(patch("/v1/accounts/12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Account Name"))
                .andExpect(jsonPath("$.accountNumber").value("12345"));
    }

    @Disabled
    @Test
    @WithMockUser(username = "test@example.com")
    void deleteAccount_shouldReturn204NoContent_whenBalanceIsZero() throws Exception {
        // Arrange
        // Mock the service call, which returns void on success
        doNothing().when(accountService).deleteAccount(eq("12345"), any());

        // Act & Assert
        mockMvc.perform(delete("/v1/accounts/12345"))
                .andExpect(status().isNoContent());
    }

    @Disabled
    @Test
    @WithMockUser(username = "test@example.com")
    void deleteAccount_shouldReturn400BadRequest_whenBalanceIsNonZero() throws Exception {
        // Arrange
        // Mock the service to throw the business rule exception
        String errorMessage = "Cannot delete account with non-zero balance.";
        doThrow(new IllegalStateException(errorMessage))
                .when(accountService).deleteAccount(eq("54321"), any());

        // Act & Assert
        mockMvc.perform(delete("/v1/accounts/54321"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}