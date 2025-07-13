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

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService; // Reuse for authorization and fetching

    @Override
    @Transactional
    public Transaction createTransaction(String accountNumber, CreateTransactionRequest request, UserDetails currentUser) throws InsufficientFundsException {
        Account account = accountService.getAccountByAccountNumber(accountNumber, currentUser);
        BigDecimal amount = request.getAmount();

        if (TransactionType.DEPOSIT.toString().equalsIgnoreCase(request.getType())) {
            account.setBalance(account.getBalance().add(amount));
        } else if (TransactionType.WITHDRAWAL.toString().equalsIgnoreCase(request.getType())) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient funds for this withdrawal.");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            throw new IllegalArgumentException("Invalid transaction type specified.");
        }

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(
                TransactionType.WITHDRAWAL.toString().equalsIgnoreCase(request.getType()) ? 
                        TransactionType.WITHDRAWAL : 
                        TransactionType.DEPOSIT
        );
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
}
