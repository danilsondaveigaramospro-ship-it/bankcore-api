package com.bankcore.transaction.controller;

import com.bankcore.account.dto.DepositRequest;
import com.bankcore.account.dto.WithdrawRequest;
import com.bankcore.transaction.dto.TransactionResponse;
import com.bankcore.transaction.dto.TransferRequest;
import com.bankcore.transaction.dto.TransferResponse;
import com.bankcore.transaction.mapper.TransactionMapper;
import com.bankcore.transaction.service.TransactionService;
import com.bankcore.transaction.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Operations")
public class OperationController {

    private final TransactionService transactionService;
    private final TransferService transferService;

    @PostMapping("/api/v1/accounts/{accountId}/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
    @Operation(summary = "Administrative deposit")
    public TransactionResponse deposit(@PathVariable UUID accountId, @Valid @RequestBody DepositRequest request) {
        return TransactionMapper.toResponse(transactionService.deposit(accountId, request));
    }

    @PostMapping("/api/v1/accounts/{accountId}/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE','ADMIN')")
    @Operation(summary = "Administrative withdrawal")
    public TransactionResponse withdraw(@PathVariable UUID accountId, @Valid @RequestBody WithdrawRequest request) {
        return TransactionMapper.toResponse(transactionService.withdraw(accountId, request));
    }

    @PostMapping("/api/v1/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Internal transfer. Customers may transfer only from their own source account.")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        TransferService.TransferResult result = transferService.transfer(request);
        return new TransferResponse(TransactionMapper.toResponse(result.outgoing()), TransactionMapper.toResponse(result.incoming()));
    }
}
