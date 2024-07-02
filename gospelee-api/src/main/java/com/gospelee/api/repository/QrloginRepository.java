package com.gospelee.api.repository;

import com.gospelee.api.entity.QrLogin;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface QrloginRepository extends JpaRepository<QrLogin, Long> {

  QrLogin findByEmailAndCode(String email, String code);

  QrLogin findByEmailAndUpdateUserAndCode(String email, String updateUser, String code);

  @Modifying
  @Transactional
  @Query("UPDATE QrLogin l SET l.token = :token, l.updateUser = :updateUser, l.updateTime = :updateTime WHERE l.uid = :uid")
  QrLogin updateQrLoginSuccess(
      @Param("uid") Long uid,
      @Param("token") String token,
      @Param("updateUser") String updateUser,
      @Param("updateTime") LocalDateTime updateTime);
}
