package com.eaglebank.api.controller;

import com.eaglebank.api.dto.Account.CreateAccountRequest;
import com.eaglebank.api.dto.Account.GetAccountResponse;
import com.eaglebank.api.dto.Account.ListAccountResponse;
import com.eaglebank.api.dto.Account.UpdateAccountRequest;
import com.eaglebank.api.mapper.AccountMapper;
import com.eaglebank.api.model.Account.Account;
import com.eaglebank.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping
    public ResponseEntity<GetAccountResponse> createAccount(
            @RequestBody CreateAccountRequest createAccountRequest,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Account account = accountService.createAccount(createAccountRequest, currentUser);
        return new ResponseEntity<>(accountMapper.toResponse(account), HttpStatus.CREATED);

    }

    @GetMapping("{accountId}")
    public ResponseEntity<GetAccountResponse> getAccount(
            @PathVariable String accountId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Account account = accountService.getAccountByAccountNumber(accountId, currentUser);
        return ResponseEntity.ok(accountMapper.toResponse(account));
    }

    @GetMapping()
    public ResponseEntity<ListAccountResponse> getAccount(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        List<Account> account = accountService.getAccounts(currentUser);
        return ResponseEntity.ok(accountMapper.toResponseList(account));
    }


    @PatchMapping("/{accountNumber}")
    public ResponseEntity<GetAccountResponse> updateAccount(
            @PathVariable String accountNumber,
            @RequestBody UpdateAccountRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Account updatedAccount = accountService.updateAccount(accountNumber, request, currentUser);
        return ResponseEntity.ok(accountMapper.toResponse(updatedAccount));
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        accountService.deleteAccount(accountNumber, currentUser);
        return ResponseEntity.noContent().build();
    }
}
