package com.eaglebank.api.dto.Transaction;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    private String toAccountNumber;
    private BigDecimal amount;
    private String description;
}