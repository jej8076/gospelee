package com.gospelee.api.service;

import com.gospelee.api.entity.QrLogin;

public interface QrloginService {

  QrLogin saveQrlogin(String email);

  QrLogin getQrLogin(String email, String code);
}
