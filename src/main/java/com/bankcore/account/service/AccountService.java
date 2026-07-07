package com.bankcore.account.service;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.account.dto.CreateAccountRequest;
import com.bankcore.account.repository.BankAccountRepository;
import com.bankcore.audit.service.AuditService;
import com.bankcore.common.enums.AccountStatus;
import com.bankcore.common.enums.KycStatus;
import com.bankcore.common.enums.UserRole;
import com.bankcore.common.exception.AccountClosedException;
import com.bankcore.common.exception.DuplicateResourceException;
import com.bankcore.common.exception.ResourceNotFoundException;
import com.bankcore.common.exception.ValidationException;
import com.bankcore.common.util.MoneyUtils;
import com.bankcore.customer.domain.CustomerProfile;
import com.bankcore.customer.repository.CustomerProfileRepository;
import com.bankcore.security.AuthenticatedUser;
import com.bankcore.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final String SWISS_BANK_CLEARING = "00762";

    private final BankAccountRepository accountRepository;
    private final CustomerProfileRepository customerRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public BankAccount createAccount(CreateAccountRequest request) {
        CustomerProfile customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (customer.getKycStatus() != KycStatus.VERIFIED) {
            throw new ValidationException("Customer KYC must be VERIFIED before opening an account");
        }
        BigDecimal dailyLimit = MoneyUtils.normalize(request.dailyTransferLimit());
        BankAccount account = BankAccount.builder()
                .customer(customer)
                .iban(generateUniqueIban())
                .accountNumber(generateUniqueAccountNumber())
                .currency(request.currency())
                .balance(BigDecimal.ZERO.setScale(2))
                .status(AccountStatus.ACTIVE)
                .accountType(request.accountType())
                .dailyTransferLimit(dailyLimit)
                .build();
        accountRepository.save(account);
        auditService.record(currentUserService.currentUser().id(), "ACCOUNT_CREATED", "BankAccount", account.getId().toString(), "{\"customerId\":\"" + customer.getId() + "\"}");
        return account;
    }

    @Transactional(readOnly = true)
    public List<BankAccount> listAccounts() {
        AuthenticatedUser user = currentUserService.currentUser();
        if (user.role() == UserRole.ROLE_CUSTOMER) {
            return accountRepository.findByCustomer_User_Id(user.id());
        }
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BankAccount getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Transactional(readOnly = true)
    public List<BankAccount> getAccountsByCustomer(UUID customerId) {
        return accountRepository.findByCustomer_Id(customerId);
    }

    @Transactional
    public BankAccount freeze(UUID accountId, String reason) {
        BankAccount account = getAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed account cannot be frozen");
        }
        account.setStatus(AccountStatus.FROZEN);
        auditService.record(currentUserService.currentUser().id(), "ACCOUNT_FROZEN", "BankAccount", accountId.toString(), "{\"reason\":\"" + sanitize(reason) + "\"}");
        return account;
    }

    @Transactional
    public BankAccount unfreeze(UUID accountId, String reason) {
        BankAccount account = getAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed account cannot be unfrozen");
        }
        account.setStatus(AccountStatus.ACTIVE);
        auditService.record(currentUserService.currentUser().id(), "ACCOUNT_UNFROZEN", "BankAccount", accountId.toString(), "{\"reason\":\"" + sanitize(reason) + "\"}");
        return account;
    }

    @Transactional
    public BankAccount close(UUID accountId) {
        BankAccount account = getAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Account is already closed");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Account can be closed only when balance is zero");
        }
        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(Instant.now());
        auditService.record(currentUserService.currentUser().id(), "ACCOUNT_CLOSED", "BankAccount", accountId.toString(), "{}");
        return account;
    }

    private String generateUniqueAccountNumber() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String accountNumber = "10" + randomDigits(8);
            if (!accountRepository.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }
        }
        throw new DuplicateResourceException("Could not generate a unique account number");
    }

    private String generateUniqueIban() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String bban = SWISS_BANK_CLEARING + randomDigits(12);
            String iban = "CH" + computeSwissCheckDigits(bban) + bban;
            if (!accountRepository.existsByIban(iban)) {
                return iban;
            }
        }
        throw new DuplicateResourceException("Could not generate a unique IBAN");
    }

    private String computeSwissCheckDigits(String bban) {
        String rearranged = bban + "121700";
        int remainder = 0;
        for (int i = 0; i < rearranged.length(); i++) {
            remainder = (remainder * 10 + Character.digit(rearranged.charAt(i), 10)) % 97;
        }
        int check = 98 - remainder;
        return String.format("%02d", check);
    }

    private String randomDigits(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }
}
