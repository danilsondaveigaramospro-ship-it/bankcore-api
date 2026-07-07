package com.bankcore.transaction.repository;

import com.bankcore.common.enums.TransactionStatus;
import com.bankcore.common.enums.TransactionType;
import com.bankcore.transaction.domain.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {

    @Query("""
            select t from BankTransaction t
            where t.sourceAccountId = :accountId or t.targetAccountId = :accountId
            order by t.createdAt desc
            """)
    List<BankTransaction> findByAccountId(@Param("accountId") UUID accountId);

    @Query("""
            select t from BankTransaction t
            where t.sourceAccountId = :accountId or t.targetAccountId = :accountId
            order by t.createdAt desc
            """)
    Page<BankTransaction> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    long countByCreatedAtBetween(Instant from, Instant to);

    long countByCreatedAtBetweenAndStatus(Instant from, Instant to, TransactionStatus status);

    @Query(value = """
            select coalesce(sum(amount), 0) from bank_transactions
            where source_account_id = :accountId
              and type = 'TRANSFER_OUT'
              and status = 'COMPLETED'
              and created_at >= :from
            """, nativeQuery = true)
    BigDecimal sumCompletedOutgoingTransfersSince(@Param("accountId") UUID accountId, @Param("from") Instant from);

    long countBySourceAccountIdAndTypeAndStatusAndCreatedAtAfter(
            UUID sourceAccountId,
            TransactionType type,
            TransactionStatus status,
            Instant after
    );

    @Query(value = """
            select count(*) from bank_transactions
            where (source_account_id = :accountId or target_account_id = :accountId)
              and status in ('FAILED', 'REJECTED')
              and created_at >= :after
            """, nativeQuery = true)
    long countFailedForAccountSince(@Param("accountId") UUID accountId, @Param("after") Instant after);
}
