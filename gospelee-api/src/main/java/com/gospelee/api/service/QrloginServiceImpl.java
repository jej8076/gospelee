package com.gospelee.api.service;

import static com.gospelee.api.utils.RandomStringGenerator.makeQrLoginRandomCode;

import com.gospelee.api.dto.qrlogin.QrLoginDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.QrLogin;
import com.gospelee.api.repository.QrloginRepository;
import com.gospelee.api.utils.TimeUtils;
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
  public boolean updateQrlogin(Account account, String code) {
    QrLogin qrLogin = qrloginRepository.findByEmailAndCode(account.getEmail(), code);
    return qrloginRepository.updateQrLoginSuccess(qrLogin.getUid(), account.getId_token(),
        account.getEmail(),
        TimeUtils.now()) == 1;
  }

  @Override
  public QrLogin getQrLogin(String email, String code) {
    return qrloginRepository.findByEmailAndCode(email, code);
  }

}


