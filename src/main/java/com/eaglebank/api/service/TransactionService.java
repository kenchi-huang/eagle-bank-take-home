package com.eaglebank.api.service;

import com.eaglebank.api.dto.Transaction.CreateTransactionRequest;
import com.eaglebank.api.exception.InsufficientFundsException;
import com.eaglebank.api.model.Transaction.Transaction;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface TransactionService {
    Transaction createTransaction(String accountNumber, CreateTransactionRequest request, UserDetails currentUser) throws InsufficientFundsException;
    List<Transaction> getTransactionsForAccount(String accountNumber, UserDetails currentUser);
    Transaction getTransactionById(String transactionId, String accountNumber, UserDetails currentUser);
}
