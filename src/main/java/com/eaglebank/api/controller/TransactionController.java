package com.eaglebank.api.controller;

import com.eaglebank.api.dto.Transaction.CreateTransactionRequest;
import com.eaglebank.api.dto.Transaction.ListTransactionsResponse;
import com.eaglebank.api.dto.Transaction.TransactionResponse;
import com.eaglebank.api.exception.InsufficientFundsException;
import com.eaglebank.api.mapper.TransactionMapper;
import com.eaglebank.api.model.Transaction.Transaction;
import com.eaglebank.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @PostMapping()
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable String accountNumber,
            @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) throws InsufficientFundsException {
        Transaction transaction = transactionService.createTransaction(accountNumber, request, currentUser);
        return new ResponseEntity<>(transactionMapper.toResponse(transaction), HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<ListTransactionsResponse> getTransactions(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountNumber, currentUser);
        return ResponseEntity.ok(transactionMapper.toResponseList(transactions));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable String accountNumber,
            @PathVariable String transactionId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Transaction transaction = transactionService.getTransactionById(transactionId, accountNumber, currentUser);
        return ResponseEntity.ok(transactionMapper.toResponse(transaction));
    }
}
