package com.gospelee.api.repository;

import com.gospelee.api.entity.QrLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrloginRepository extends JpaRepository<QrLogin, Long> {

  QrLogin findByEmailAndCode(String email, String code);

}
