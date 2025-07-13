package com.eaglebank.api.service.impl;

import com.eaglebank.api.dto.Account.CreateAccountRequest;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.User.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.Impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.MissingResourceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;
    private UserDetails currentUserDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@example.com");

        currentUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void createAccount_shouldSetDefaultsAndSave() {
        // Arrange
        var request = new CreateAccountRequest();
        request.setName("My Savings");
        request.setAccountType(com.eaglebank.api.model.Account.AccountType.PERSONAL);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Account createdAccount = accountService.createAccount(request, currentUserDetails);

        // Assert
        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getName()).isEqualTo("My Savings");
        assertThat(createdAccount.getUser()).isEqualTo(testUser);
        assertThat(createdAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(createdAccount.getCurrency()).isEqualTo("GBP");
        assertThat(createdAccount.getSortCode()).isEqualTo("10-10-10");
        assertThat(createdAccount.getAccountNumber()).isNotNull().hasSize(8);

        // Use ArgumentCaptor to verify the object passed to save
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getName()).isEqualTo("My Savings");
    }

    @Test
    void getAccountByAccountNumber_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // Arrange
        var otherUser = new User();
        otherUser.setEmail("other@example.com");

        var account = new Account();
        account.setAccountNumber("9999");
        account.setUser(otherUser); // Account owned by someone else

        when(accountRepository.findByAccountNumber("9999")).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            accountService.getAccountByAccountNumber("9999", currentUserDetails);
        });
    }

    @Test
    void getAccountByAccountNumber_shouldThrowNotFound_whenAccountDoesNotExist() {
        // Arrange
        when(accountRepository.findByAccountNumber("1234")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MissingResourceException.class, () -> {
            accountService.getAccountByAccountNumber("1234", currentUserDetails);
        });
    }
}