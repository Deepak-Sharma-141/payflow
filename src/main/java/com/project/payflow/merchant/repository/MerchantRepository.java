package com.project.payflow.merchant.repository;

import com.project.payflow.merchant.entity.Merchant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    boolean existsByEmail(@Email @NotNull(message = "Email is required") String email);

}
