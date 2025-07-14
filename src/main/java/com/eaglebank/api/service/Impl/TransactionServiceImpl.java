package com.eaglebank.api.service.Impl;

import com.eaglebank.api.dto.Transaction.CreateTransactionRequest;
import com.eaglebank.api.exception.InsufficientFundsException;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.model.Transaction.Transaction;
import com.eaglebank.api.model.Transaction.TransactionType;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
import com.eaglebank.api.service.AccountService;
import com.eaglebank.api.service.TransactionService;
import jakarta.transaction.Transactional;
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
    private final AccountService accountService; // Reuse for authorisation and fetching

    @Override
    @Transactional
    public Transaction createTransaction(String accountNumber, CreateTransactionRequest request, UserDetails currentUser) throws InsufficientFundsException {
        Account account = accountService.getAccountByAccountNumber(accountNumber, currentUser);
        BigDecimal amount = request.getAmount();

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type specified. Must be 'DEPOSIT' or 'WITHDRAWAL'.");
        }

        switch (transactionType) {
            case DEPOSIT:
                account.setBalance(account.getBalance().add(amount));
                break;
            case WITHDRAWAL:
                if (account.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException("Insufficient funds for this withdrawal.");
                }
                account.setBalance(account.getBalance().subtract(amount));
                break;
        }

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(transactionType);
        transaction.setAmount(amount);
        transaction.setCurrency(request.getCurrency());
        transaction.setDescription(request.getReference());

        transactionRepository.save(transaction);
        accountRepository.save(account);

        return transaction;
    }

    @Override
    public List<Transaction> getTransactionsForAccount(String accountNumber, UserDetails currentUser) {
        Account account = accountService.getAccountByAccountNumber(accountNumber, currentUser);
        return transactionRepository.findByAccountOrderByCreatedTimestampDesc(account);
    }

    @Override
    public Transaction getTransactionById(String transactionId, String accountNumber, UserDetails currentUser) {
        accountService.getAccountByAccountNumber(accountNumber, currentUser);
        return transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getAccount().getAccountNumber().equals(accountNumber))
                .orElseThrow(() -> new MissingResourceException("Transaction not found", "Transaction", transactionId));
    }
}
