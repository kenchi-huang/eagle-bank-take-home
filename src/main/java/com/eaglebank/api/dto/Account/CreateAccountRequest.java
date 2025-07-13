package com.eaglebank.api.dto.Account;

import com.eaglebank.api.model.Account.AccountType;
import lombok.Data;

@Data
public class CreateAccountRequest {
    private String name;
    private AccountType accountType;
}
