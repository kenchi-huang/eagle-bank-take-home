package com.eaglebank.api.mapper;

import com.eaglebank.api.dto.Account.GetAccountResponse;
import com.eaglebank.api.dto.Account.ListAccountResponse;
import com.eaglebank.api.model.Account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountMapper {

    private final UserMapper userMapper;

    public GetAccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }
        return GetAccountResponse.builder()
                .id(account.getId())
                .user(userMapper.toResponse(account.getUser()))
                .accountNumber(account.getAccountNumber())
                .sortCode(account.getSortCode())
                .name(account.getName())
                .accountType(String.valueOf(account.getAccountType()))
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .created(account.getCreatedTimestamp())
                .lastUpdated(account.getUpdatedTimestamp())
                .build();
    }

    public ListAccountResponse toResponseList(List<Account> accounts) {
        return ListAccountResponse.builder()
                .accounts(accounts.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList())
                )
                .build();
    }
}
