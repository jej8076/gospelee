package com.gospelee.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gospelee.api.entity.Account;
import java.util.Optional;

public interface AccountService {

  Optional<Account> getAccountByPhone(final String Phone);

  Optional<Account> saveAccount(Account account) throws JsonProcessingException;

  Optional<Account> getAccountByToken(String token);

  String validationIdToken(String idToken);
}
