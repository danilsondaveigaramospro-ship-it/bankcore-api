package com.bankcore.customer.repository;

import com.bankcore.customer.domain.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
    Optional<CustomerProfile> findByUser_Id(UUID userId);

    boolean existsByUser_Id(UUID userId);

    boolean existsByIdAndUser_Id(UUID id, UUID userId);
}
