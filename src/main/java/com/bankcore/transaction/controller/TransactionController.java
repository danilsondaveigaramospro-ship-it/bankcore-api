package com.bankcore.transaction.controller;

import com.bankcore.account.mapper.AccountMapper;
import com.bankcore.account.service.AccountService;
import com.bankcore.transaction.dto.StatementResponse;
import com.bankcore.transaction.dto.TransactionResponse;
import com.bankcore.transaction.mapper.TransactionMapper;
import com.bankcore.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @GetMapping("/transactions")
    @Operation(summary = "List transactions. Customers receive only transactions touching their accounts.")
    public List<TransactionResponse> listTransactions() {
        return transactionService.listTransactions().stream().map(TransactionMapper::toResponse).toList();
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get transaction details with account ownership checks")
    public TransactionResponse getTransaction(@PathVariable UUID transactionId) {
        return TransactionMapper.toResponse(transactionService.getTransaction(transactionId));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @PreAuthorize("@accountAccessPolicy.canAccessAccount(#accountId)")
    @Operation(summary = "List account transactions")
    public List<TransactionResponse> getAccountTransactions(@PathVariable UUID accountId) {
        return transactionService.getTransactionsForAccount(accountId).stream().map(TransactionMapper::toResponse).toList();
    }

    @GetMapping("/accounts/{accountId}/statement")
    @PreAuthorize("@accountAccessPolicy.canAccessAccount(#accountId)")
    @Operation(summary = "Return a simple bank statement payload")
    public StatementResponse statement(@PathVariable UUID accountId) {
        return new StatementResponse(
                AccountMapper.toResponse(accountService.getAccount(accountId)),
                Instant.now(),
                transactionService.getTransactionsForAccount(accountId).stream().map(TransactionMapper::toResponse).toList()
        );
    }
}
