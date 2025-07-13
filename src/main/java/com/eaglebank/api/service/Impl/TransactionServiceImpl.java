package com.eaglebank.api.service.Impl;

import com.eaglebank.api.dto.Transaction.CreateTransactionRequest;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.Transaction.Transaction;
import com.eaglebank.api.model.Transaction.TransactionType;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
import com.eaglebank.api.service.AccountService;
import com.eaglebank.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.MissingResourceException;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService; // Reuse for authorization and fetching

    @Override
    public Transaction createTransaction(String accountNumber, CreateTransactionRequest request, UserDetails currentUser) {
        Account fromAccount = accountService.getAccountByAccountNumber(accountNumber, currentUser);

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new MissingResourceException("Destination Account not found", "Account", request.toString()));

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Amount must be greater than zero");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        Transaction debitTransaction = new Transaction();
        debitTransaction.setAccount(fromAccount);
        debitTransaction.setType(TransactionType.DEBIT);
        debitTransaction.setAmount(amount);
        debitTransaction.setCurrency(fromAccount.getCurrency());
        debitTransaction.setDescription("Transfer to: " + toAccount.getAccountNumber());
        transactionRepository.save(debitTransaction);

        Transaction creditTransaction = new Transaction();
        creditTransaction.setAccount(toAccount);
        creditTransaction.setType(TransactionType.CREDIT);
        creditTransaction.setAmount(amount);
        creditTransaction.setCurrency(fromAccount.getCurrency());
        creditTransaction.setDescription("Transfer from: " + fromAccount.getAccountNumber());
        transactionRepository.save(creditTransaction);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return debitTransaction;
    }

    @Override
    public List<Transaction> getTransactionsForAccount(String accountNumber, UserDetails currentUser) {
        Account account = accountService.getAccountByAccountNumber(accountNumber, currentUser);
        return transactionRepository.findByAccountOrderByCreatedTimestampDesc(account);
    }
}
