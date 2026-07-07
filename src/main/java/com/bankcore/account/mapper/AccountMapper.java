package com.bankcore.account.mapper;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.account.dto.AccountResponse;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountResponse toResponse(BankAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomer().getId(),
                account.getIban(),
                account.getAccountNumber(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getAccountType(),
                account.getDailyTransferLimit(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                account.getClosedAt()
        );
    }
}
