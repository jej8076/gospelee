package com.gospelee.api.service;

import static com.gospelee.api.utils.RandomStringGenerator.makeQrLoginRandomCode;

import com.gospelee.api.dto.qrlogin.QrLoginDTO;
import com.gospelee.api.entity.QrLogin;
import com.gospelee.api.repository.QrloginRepository;
import org.springframework.stereotype.Service;

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


