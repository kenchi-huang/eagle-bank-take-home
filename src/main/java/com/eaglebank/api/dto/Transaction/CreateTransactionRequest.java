package com.eaglebank.api.dto.Transaction;

import com.eaglebank.api.model.Transaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    private String type;
    private BigDecimal amount;
    private String reference;
    private String currency;
}