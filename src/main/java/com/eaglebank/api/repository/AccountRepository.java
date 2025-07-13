package com.eaglebank.api.repository;

import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByUser(User user);
    Optional<Account> findByAccountNumber(String accountNumber);
}