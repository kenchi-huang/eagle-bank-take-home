package com.eaglebank.api.service;

import com.eaglebank.api.dto.Account.CreateAccountRequest;
import com.eaglebank.api.dto.Account.UpdateAccountRequest;
import com.eaglebank.api.model.Account.Account;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface AccountService {
    Account createAccount(CreateAccountRequest request, UserDetails currentUser);
    List<Account> getAccounts(UserDetails currentUser);
    Account getAccountByAccountNumber(String accountNumber, UserDetails currentUser);
    Account updateAccount(String accountNumber, UpdateAccountRequest request, UserDetails currentUser);
    void deleteAccount(String accountNumber, UserDetails currentUser);
}
