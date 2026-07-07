package com.bankcore.account.repository;

import com.bankcore.account.domain.BankAccount;
import com.bankcore.common.enums.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    List<BankAccount> findByCustomer_Id(UUID customerId);

    List<BankAccount> findByCustomer_User_Id(UUID userId);

    boolean existsByIban(String iban);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByIdAndCustomer_User_Id(UUID id, UUID userId);

    long countByStatus(AccountStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a where a.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a where a.id in :ids order by a.id")
    List<BankAccount> findAllByIdInForUpdate(@Param("ids") Collection<UUID> ids);

    @Query(value = "select currency, coalesce(sum(balance), 0) from bank_accounts where status <> 'CLOSED' group by currency", nativeQuery = true)
    List<Object[]> totalBalanceByCurrency();

    @Query(value = "select coalesce(sum(balance), 0) from bank_accounts where currency = :currency and status <> 'CLOSED'", nativeQuery = true)
    BigDecimal totalBalanceForCurrency(@Param("currency") String currency);
}
