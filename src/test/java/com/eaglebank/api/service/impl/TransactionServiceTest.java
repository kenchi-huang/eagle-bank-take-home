package com.eaglebank.api.service.impl;

import com.eaglebank.api.dto.Transaction.CreateTransactionRequest;
import com.eaglebank.api.exception.InsufficientFundsException;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.Transaction.Transaction;
import com.eaglebank.api.model.Transaction.TransactionType;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
import com.eaglebank.api.service.AccountService;
import com.eaglebank.api.service.Impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account testAccount;
    private UserDetails currentUserDetails;

    @BeforeEach
    void setUp() {
        currentUserDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();

        testAccount = new Account();
        testAccount.setAccountNumber("12345678");
        testAccount.setBalance(new BigDecimal("500.00"));
        testAccount.setCurrency("GBP");
    }

    @Test
    void createTransaction_shouldSucceed_forDeposit() throws InsufficientFundsException {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setType("deposit");
        request.setAmount(new BigDecimal("150.50"));
        request.setCurrency("GBP");
        request.setReference("Cash deposit");

        when(accountService.getAccountByAccountNumber("12345678", currentUserDetails)).thenReturn(testAccount);

        // Act
        transactionService.createTransaction("12345678", request, currentUserDetails);

        // Assert
        // 1. Verify balance was increased
        assertThat(testAccount.getBalance()).isEqualByComparingTo("650.50");

        // 2. Capture and verify the transaction record
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        verify(accountRepository).save(testAccount); // Verify the account was saved

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo("150.50");
        assertThat(savedTransaction.getDescription()).isEqualTo("Cash deposit");
    }

    @Test
    void createTransaction_shouldSucceed_forWithdrawalWhenFundsAreSufficient() throws InsufficientFundsException {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setType("withdrawal");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("GBP");

        when(accountService.getAccountByAccountNumber("12345678", currentUserDetails)).thenReturn(testAccount);

        // Act
        transactionService.createTransaction("12345678", request, currentUserDetails);

        // Assert
        // 1. Verify balance was decreased
        assertThat(testAccount.getBalance()).isEqualByComparingTo("400.00");

        // 2. Capture and verify the transaction record
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        verify(accountRepository).save(testAccount);

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void createTransaction_shouldFail_forWithdrawalWithInsufficientFunds() {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setType("withdrawal");
        request.setAmount(new BigDecimal("1000.00")); // More than the balance

        when(accountService.getAccountByAccountNumber("12345678", currentUserDetails)).thenReturn(testAccount);

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            transactionService.createTransaction("12345678", request, currentUserDetails);
        }, "Insufficient funds for this withdrawal.");

        // Verify no state-changing methods were called
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createTransaction_shouldFail_forInvalidTransactionType() {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setType("transfer"); // Invalid type
        request.setAmount(new BigDecimal("50.00"));

        when(accountService.getAccountByAccountNumber("12345678", currentUserDetails)).thenReturn(testAccount);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction("12345678", request, currentUserDetails);
        });
    }
}