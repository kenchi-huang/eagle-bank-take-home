package com.eaglebank.api.mapper;

import com.eaglebank.api.dto.Account.GetAccountResponse;
import com.eaglebank.api.dto.Transaction.TransactionResponse;
import com.eaglebank.api.dto.User.UserResponse;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.Transaction.Transaction;
import com.eaglebank.api.model.Transaction.TransactionType;
import com.eaglebank.api.model.User.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MappersTest {

    // Mocks and Injects for AccountMapper which has a dependency
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AccountMapper accountMapper;

    @Test
    void userMapper_shouldConvertToResponse() {
        // Arrange
        var user = new User();
        user.setId("user-1");
        user.setName("John Doe");
        user.setEmail("john@test.com");
        var mapper = new UserMapper(); // No dependencies, can instantiate directly

        // Act
        UserResponse response = mapper.toResponse(user);

        // Assert
        assertThat(response.getId()).isEqualTo("user-1");
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void transactionMapper_shouldConvertToResponse() {
        // Arrange
        var transaction = new Transaction();
        transaction.setId("tx-1");
        transaction.setType(TransactionType.CREDIT);
        transaction.setAmount(new BigDecimal("100.50"));
        transaction.setCreatedTimestamp(Instant.now());
        var mapper = new TransactionMapper(); // No dependencies

        // Act
        TransactionResponse response = mapper.toResponse(transaction);

        // Assert
        assertThat(response.getId()).isEqualTo("tx-1");
        assertThat(response.getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(response.getAmount()).isEqualByComparingTo("100.50");
        assertThat(response.getCreatedTimestamp()).isEqualTo(transaction.getCreatedTimestamp());
    }

    @Test
    void accountMapper_shouldConvertToResponse() {
        // Arrange
        var user = new User();
        user.setId("user-1");
        var userResponse = UserResponse.builder().id("user-1").build();

        var account = new Account();
        account.setId("acc-1");
        account.setAccountNumber("12345");
        account.setUser(user);

        // Mock the dependency
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        GetAccountResponse response = accountMapper.toResponse(account);

        // Assert
        assertThat(response.getId()).isEqualTo("acc-1");
        assertThat(response.getAccountNumber()).isEqualTo("12345");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo("user-1");
    }
}