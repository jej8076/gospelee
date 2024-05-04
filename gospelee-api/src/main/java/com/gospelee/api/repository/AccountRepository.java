package com.gospelee.api.repository;

import com.gospelee.api.entity.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

  Optional<Account> findByPhone(String phone);

  Optional<Account> findByEmail(String email);

  Optional<List<Account>> findByEcclesiaUid(String ecclesiaUid);
}
