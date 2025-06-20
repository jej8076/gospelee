package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.entity.Account;
import java.util.List;
import java.util.Optional;

public interface AccountService {

  List<Account> getAccountAll();

  Optional<List<Account>> getAccountByEcclesiaUid(Long ecclesiaUid);

  Optional<Account> getAccountByPhone(final String Phone);

  Optional<Account> getAccountByEmail(String email);

  Optional<AccountAuthDTO> saveAndGetAccount(JwtPayload jwtPayload, String idToken);

  void savePushToken(Long uid, String pushToken);

}
