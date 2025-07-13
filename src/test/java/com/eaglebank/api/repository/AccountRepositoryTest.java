package com.eaglebank.api.repository;

import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("pass");
        entityManager.persist(user1);

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("pass");
        entityManager.persist(user2);

        Account account1 = new Account();
        account1.setAccountNumber("1001");
        account1.setUser(user1);
        // Set other required fields...
        account1.setSortCode("10-10-10");
        account1.setBalance(java.math.BigDecimal.TEN);
        account1.setCurrency("GBP");
        account1.setAccountType(com.eaglebank.api.model.Account.AccountType.PERSONAL);
        account1.setName("User1 Main");
        entityManager.persist(account1);

        Account account2 = new Account();
        account2.setAccountNumber("2002");
        account2.setUser(user2);
        // Set other required fields...
        account2.setSortCode("10-10-10");
        account2.setBalance(java.math.BigDecimal.ZERO);
        account2.setCurrency("GBP");
        account2.setAccountType(com.eaglebank.api.model.Account.AccountType.PERSONAL);
        account2.setName("User2 Main");
        entityManager.persist(account2);

        entityManager.flush();
    }

    @Test
    void findByUser_shouldReturnOnlyUserAccounts() {
        // Act
        List<Account> foundAccounts = accountRepository.findByUser(user1);

        // Assert
        assertThat(foundAccounts).hasSize(1);
        assertThat(foundAccounts.get(0).getAccountNumber()).isEqualTo("1001");
    }

    @Test
    void findByAccountNumber_shouldReturnCorrectAccount() {
        // Act
        Optional<Account> foundAccount = accountRepository.findByAccountNumber("2002");

        // Assert
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getUser().getEmail()).isEqualTo("user2@example.com");
    }
}