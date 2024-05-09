package com.gospelee.api.repository;

import com.gospelee.api.entity.Account;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AccountRepository extends JpaRepository<Account, String> {

  Optional<Account> findByPhone(String phone);

  Optional<Account> findByEmail(String email);

  Optional<List<Account>> findByEcclesiaUid(String ecclesiaUid);

  @Modifying
  @Transactional
  @Query("UPDATE Account a SET a.id_token = :idToken, a.updateTime = :updateTime WHERE a.uid = :uid")
  void updateAccountIdTokenByUid(@Param("uid") Long uid, @Param("idToken") String idToken,
      @Param("updateTime") LocalDateTime updateTime);

  default Account updateAccountIdTokenAndFindById(Long uid, String idToken) {
    updateAccountIdTokenByUid(uid, idToken, LocalDateTime.now());
    return findById(String.valueOf(uid)).orElse(null);
  }
}
