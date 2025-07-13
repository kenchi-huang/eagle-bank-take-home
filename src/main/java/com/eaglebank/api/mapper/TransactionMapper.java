package com.eaglebank.api.mapper;

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
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .createdTimestamp(transaction.getCreatedTimestamp())
                .description(transaction.getDescription())
                .currency(transaction.getCurrency())
                .build();
    }

    public List<TransactionResponse> toResponseList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
