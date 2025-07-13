package com.eaglebank.api.dto.Account;

import com.eaglebank.api.dto.User.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class GetAccountResponse {
    private String id;
    private UserResponse user;
    private String name;
    private String accountNumber;
    private String sortCode;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private Instant created;
    private Instant lastUpdated;
}
