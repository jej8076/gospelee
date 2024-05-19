package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.entity.Account;
import java.util.List;
import java.util.Optional;

public interface AccountService {

  List<Account> getAccountAll();

  Optional<List<Account>> getAccountByEcclesiaUid(String ecclesiaUid);

  Optional<Account> getAccountByPhone(final String Phone);

  Optional<Account> getAccountByToken(String token);

  Optional<Account> saveAndGetAccount(JwtPayload jwtPayload, String idToken);

  void savePushToken(Long uid, String pushToken);
}
