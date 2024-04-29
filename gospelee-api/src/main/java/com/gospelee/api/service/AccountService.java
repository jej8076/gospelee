package com.gospelee.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.entity.Account;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;

public interface AccountService {

  List<Account> getAccountAll();

  Optional<Account> getAccountByPhone(final String Phone);

  Optional<Account> getAccountByToken(String token);

  Optional<Account> saveAndGetAccount(JwtPayload jwtPayload);
}
