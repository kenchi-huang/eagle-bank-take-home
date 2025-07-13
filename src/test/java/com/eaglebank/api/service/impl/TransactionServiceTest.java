package com.eaglebank.api.service.impl;

import com.eaglebank.api.dto.Transaction.CreateTransactionRequest;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.Transaction.Transaction;
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
import java.util.MissingResourceException;
import java.util.Optional;

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

    private Account fromAccount;
    private Account toAccount;
    private UserDetails currentUserDetails;

    @BeforeEach
    void setUp() {
        currentUserDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();

        fromAccount = new Account();
        fromAccount.setAccountNumber("1111");
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount.setCurrency("GBP");

        toAccount = new Account();
        toAccount.setAccountNumber("2222");
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setCurrency("GBP");
    }

    @Test
    void createTransaction_shouldSucceed_whenFundsAreSufficient() {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setToAccountNumber("2222");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Payment for services");

        // Mock the authorization and account retrieval
        when(accountService.getAccountByAccountNumber("1111", currentUserDetails)).thenReturn(fromAccount);
        when(accountRepository.findByAccountNumber("2222")).thenReturn(Optional.of(toAccount));

        // Act
        transactionService.createTransaction("1111", request, currentUserDetails);

        // Assert
        // 1. Verify balances are updated correctly
        assertThat(fromAccount.getBalance()).isEqualByComparingTo("900.00");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("600.00");

        // 2. Capture the two transaction records that should have been saved
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        verify(accountRepository, times(2)).save(any(Account.class));

        Transaction debit = transactionCaptor.getAllValues().get(0);
        Transaction credit = transactionCaptor.getAllValues().get(1);

        // 3. Verify the DEBIT transaction
        assertThat(debit.getAccount().getAccountNumber()).isEqualTo("1111");
        assertThat(debit.getType()).isEqualTo(com.eaglebank.api.model.Transaction.TransactionType.DEBIT);
        assertThat(debit.getAmount()).isEqualByComparingTo("100.00");
        assertThat(debit.getDescription()).isEqualTo("Transfer to: 2222");

        // 4. Verify the CREDIT transaction
        assertThat(credit.getAccount().getAccountNumber()).isEqualTo("2222");
        assertThat(credit.getType()).isEqualTo(com.eaglebank.api.model.Transaction.TransactionType.CREDIT);
        assertThat(credit.getAmount()).isEqualByComparingTo("100.00");
        assertThat(credit.getDescription()).isEqualTo("Transfer from: 1111");
    }

    @Test
    void createTransaction_shouldFail_whenFundsAreInsufficient() {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setToAccountNumber("2222");
        request.setAmount(new BigDecimal("2000.00")); // More than the balance

        when(accountService.getAccountByAccountNumber("1111", currentUserDetails)).thenReturn(fromAccount);
        when(accountRepository.findByAccountNumber("2222")).thenReturn(Optional.of(toAccount));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            transactionService.createTransaction("1111", request, currentUserDetails);
        }, "Insufficient funds for this transaction.");

        // Verify no interactions that would change state occurred
        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createTransaction_shouldFail_whenDestinationAccountNotFound() {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setToAccountNumber("9999"); // Non-existent account
        request.setAmount(new BigDecimal("50.00"));

        when(accountService.getAccountByAccountNumber("1111", currentUserDetails)).thenReturn(fromAccount);
        when(accountRepository.findByAccountNumber("9999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MissingResourceException.class, () -> {
            transactionService.createTransaction("1111", request, currentUserDetails);
        });
    }

    @Test
    void createTransaction_shouldFail_whenAmountIsNegative() {
        // Arrange
        var request = new CreateTransactionRequest();
        request.setToAccountNumber("2222");
        request.setAmount(new BigDecimal("-50.00")); // Invalid amount

        when(accountService.getAccountByAccountNumber("1111", currentUserDetails)).thenReturn(fromAccount);
        when(accountRepository.findByAccountNumber("2222")).thenReturn(Optional.of(toAccount));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            transactionService.createTransaction("1111", request, currentUserDetails);
        }, "Transaction amount must be positive.");
    }
}