package com.gospelee.api.service;

import static com.gospelee.api.utils.RandomStringGenerator.makeQrLoginRandomCode;

import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.dto.qrlogin.QrLoginDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.QrLogin;
import com.gospelee.api.entity.RoleType;
import com.gospelee.api.repository.AccountKakaoTokenRepository;
import com.gospelee.api.repository.AccountRepository;
import com.gospelee.api.repository.QrloginRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class QrloginServiceImpl implements QrloginService {

  private final QrloginRepository qrloginRepository;

  public QrloginServiceImpl(QrloginRepository qrloginRepository) {
    this.qrloginRepository = qrloginRepository;
  }

  @Override
  public QrLogin saveQrlogin(String email) {
    String code = makeQrLoginRandomCode();
    QrLogin qrLogin = QrLoginDTO.Request.toEntity(email, code);
    return qrloginRepository.save(qrLogin);
  }

  @Override
  public QrLogin getQrLogin(String email, String code) {
    return qrloginRepository.findByEmailAndCode(email, code);
  }
}


