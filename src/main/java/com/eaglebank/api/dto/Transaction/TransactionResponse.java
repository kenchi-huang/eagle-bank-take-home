package com.eaglebank.api.dto.Transaction;


import com.eaglebank.api.model.Transaction.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionResponse {
    private String id;
    private String userId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private String reference;
    private Instant createdTimestamp;
}
