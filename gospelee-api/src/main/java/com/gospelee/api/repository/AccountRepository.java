package com.gospelee.api.repository;

import com.gospelee.api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByPhone(String phone);
}
