package com.bankcore.account.controller;

import com.bankcore.account.dto.AccountResponse;
import com.bankcore.account.dto.CreateAccountRequest;
import com.bankcore.account.dto.FreezeAccountRequest;
import com.bankcore.account.mapper.AccountMapper;
import com.bankcore.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/api/v1/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
    @Operation(summary = "Create a bank account for a VERIFIED customer")
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return AccountMapper.toResponse(accountService.createAccount(request));
    }

    @GetMapping("/api/v1/accounts")
    @Operation(summary = "List accounts. Customers receive only their own accounts.")
    public List<AccountResponse> listAccounts() {
        return accountService.listAccounts().stream().map(AccountMapper::toResponse).toList();
    }

    @GetMapping("/api/v1/accounts/{accountId}")
    @PreAuthorize("@accountAccessPolicy.canAccessAccount(#accountId)")
    @Operation(summary = "Get account details with ownership checks")
    public AccountResponse getAccount(@PathVariable UUID accountId) {
        return AccountMapper.toResponse(accountService.getAccount(accountId));
    }

    @GetMapping("/api/v1/customers/{customerId}/accounts")
    @PreAuthorize("@customerAccessPolicy.canAccessCustomer(#customerId)")
    @Operation(summary = "List accounts for a customer")
    public List<AccountResponse> getCustomerAccounts(@PathVariable UUID customerId) {
        return accountService.getAccountsByCustomer(customerId).stream().map(AccountMapper::toResponse).toList();
    }

    @PatchMapping("/api/v1/accounts/{accountId}/freeze")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
    @Operation(summary = "Freeze an account with an audit reason")
    public AccountResponse freeze(@PathVariable UUID accountId, @Valid @RequestBody FreezeAccountRequest request) {
        return AccountMapper.toResponse(accountService.freeze(accountId, request.reason()));
    }

    @PatchMapping("/api/v1/accounts/{accountId}/unfreeze")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
    @Operation(summary = "Unfreeze an account with an audit reason")
    public AccountResponse unfreeze(@PathVariable UUID accountId, @Valid @RequestBody FreezeAccountRequest request) {
        return AccountMapper.toResponse(accountService.unfreeze(accountId, request.reason()));
    }

    @PatchMapping("/api/v1/accounts/{accountId}/close")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
    @Operation(summary = "Close a zero-balance account")
    public AccountResponse close(@PathVariable UUID accountId) {
        return AccountMapper.toResponse(accountService.close(accountId));
    }
}
