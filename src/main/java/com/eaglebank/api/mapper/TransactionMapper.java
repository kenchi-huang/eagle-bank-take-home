package com.eaglebank.api.mapper;

import com.eaglebank.api.dto.Transaction.ListTransactionsResponse;
import com.eaglebank.api.dto.Transaction.TransactionResponse;
import com.eaglebank.api.model.Transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getAccount().getUser().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .createdTimestamp(transaction.getCreatedTimestamp())
                .reference(transaction.getDescription())
                .currency(transaction.getCurrency())
                .build();
    }

    public ListTransactionsResponse toResponseList(List<Transaction> transactions) {
        return ListTransactionsResponse.builder()
                .transactions(
                        transactions.stream()
                            .map(this::toResponse)
                            .collect(Collectors.toList()))
                .build();
    }
}
