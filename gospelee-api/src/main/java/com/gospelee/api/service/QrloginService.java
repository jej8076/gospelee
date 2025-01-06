package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.QrLogin;

public interface QrloginService {

  QrLogin saveQrlogin(String email);

  boolean updateQrlogin(AccountAuthDTO account, String code);

  QrLogin getQrLogin(String email, String code);
}
