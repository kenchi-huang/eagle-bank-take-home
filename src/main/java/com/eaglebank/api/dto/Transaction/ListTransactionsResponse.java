package com.eaglebank.api.dto.Transaction;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ListTransactionsResponse {
    private List<TransactionResponse> transactions;
}
