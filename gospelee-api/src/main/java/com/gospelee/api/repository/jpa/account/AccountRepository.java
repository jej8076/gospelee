package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.Account;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AccountRepository extends JpaRepository<Account, Long> {

  Optional<Account> findByPhone(String phone);

  Optional<Account> findByEmail(String email);

  Optional<List<Account>> findByEcclesiaUid(Long ecclesiaUid);

  @Modifying
  @Transactional
  @Query("UPDATE Account a SET a.idToken = :idToken, a.updateTime = :updateTime WHERE a.uid = :uid")
  void updateAccountIdTokenByUid(@Param("uid") Long uid, @Param("idToken") String idToken,
      @Param("updateTime") LocalDateTime updateTime);

  default Account updateAccountIdTokenAndFindById(Long uid, String idToken) {
    updateAccountIdTokenByUid(uid, idToken, LocalDateTime.now());
    return findById(uid).orElse(null);
  }

  @Modifying
  @Transactional
  @Query("UPDATE Account a SET a.pushToken = :pushToken, a.updateTime = :updateTime WHERE a.uid = :uid")
  void savePushToken(@Param("uid") Long uid, @Param("pushToken") String pushToken,
      @Param("updateTime") LocalDateTime updateTime);

}
