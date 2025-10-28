package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.AccountMeta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountMetaRepository extends JpaRepository<AccountMeta, Long> {

  Optional<AccountMeta> findByEmail(String email);
}
