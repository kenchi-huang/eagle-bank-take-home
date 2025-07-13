package com.eaglebank.api.service.Impl;

import com.eaglebank.api.dto.Account.CreateAccountRequest;
import com.eaglebank.api.dto.Account.UpdateAccountRequest;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.User.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    public Account createAccount(CreateAccountRequest request, UserDetails currentUser) {
        User user = findUserByUserDetails(currentUser);

        Account newAccount = new Account();
        newAccount.setUser(user);
        newAccount.setName(request.getName());
        newAccount.setAccountType(request.getAccountType());
        newAccount.setAccountNumber(generateUniqueAccountNumber());

        newAccount.setSortCode("10-10-10");
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setCurrency("GBP");

        return accountRepository.save(newAccount);
    }

    @Override
    public List<Account> getAccounts(UserDetails currentUser) {
        User user = findUserByUserDetails(currentUser);
        return accountRepository.findByUser(user);
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber, UserDetails currentUser) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new MissingResourceException("Account not found", "Account", accountNumber));

        // Authorisation Check: Ensure the logged-in user owns this account.
        if (!account.getUser().getEmail().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("You are not authorised to access this account.");
        }

        return account;
    }

    @Override
    public Account updateAccount(String accountNumber, UpdateAccountRequest request, UserDetails currentUser) {
        // This reuses the authorization check from getAccountByAccountNumber
        Account accountToUpdate = getAccountByAccountNumber(accountNumber, currentUser);

        if (StringUtils.hasText(request.getName())) {
            accountToUpdate.setName(request.getName());
        }
        // The spec only allows updating the name, but you could add other fields here.

        return accountRepository.save(accountToUpdate);
    }

    @Override
    public void deleteAccount(String accountNumber, UserDetails currentUser) {
        Account accountToDelete = getAccountByAccountNumber(accountNumber, currentUser);
        if (accountToDelete.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot delete account with non-zero balance.");
        }
        // Add business logic here if needed (e.g., check for zero balance before deleting)
        accountRepository.delete(accountToDelete);
    }


    private User findUserByUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found for token."));
    }

    private String generateUniqueAccountNumber() {
        return "01" + ThreadLocalRandom.current().nextLong(100_000, 1_000_000);
    }
}
