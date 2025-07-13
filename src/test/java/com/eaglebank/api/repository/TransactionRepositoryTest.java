package com.eaglebank.api.repository;

import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.Account.AccountType;
import com.eaglebank.api.model.Transaction.Transaction;
import com.eaglebank.api.model.Transaction.TransactionType;
import com.eaglebank.api.model.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("pass");
        entityManager.persist(user);

        testAccount = new Account();
        testAccount.setUser(user);
        testAccount.setAccountNumber("12345678");
        testAccount.setSortCode("10-10-10");
        testAccount.setBalance(BigDecimal.TEN);
        testAccount.setCurrency("GBP");
        testAccount.setAccountType(AccountType.PERSONAL);
        testAccount.setName("Test Account");
        entityManager.persist(testAccount);

        // Create transactions with slight delay to ensure distinct timestamps
        Transaction tx1 = createTransaction("Coffee", "5.00");
        entityManager.persist(tx1);
        Transaction tx2 = createTransaction("Lunch", "15.00");
        entityManager.persist(tx2);

        entityManager.flush();
    }

    private Transaction createTransaction(String description, String amount) {
        Transaction tx = new Transaction();
        tx.setAccount(testAccount);
        tx.setType(TransactionType.DEBIT);
        tx.setAmount(new BigDecimal(amount));
        tx.setCurrency("GBP");
        tx.setDescription(description);
        return tx;
    }

    @Test
    void findByAccountOrderByCreatedTimestampDesc_shouldReturnTransactionsInCorrectOrder() {
        // Act
        List<Transaction> foundTransactions = transactionRepository.findByAccountOrderByCreatedTimestampDesc(testAccount);

        // Assert
        assertThat(foundTransactions).hasSize(2);
        // The most recent transaction ("Lunch") should be first in the list
        assertThat(foundTransactions.get(0).getDescription()).isEqualTo("Lunch");
        assertThat(foundTransactions.get(1).getDescription()).isEqualTo("Coffee");
    }
}