package com.gospelee.api.service;

import com.gospelee.api.dto.AccountVo;
import com.gospelee.api.entity.Account;

import java.util.Optional;

public interface AccountService {
    Optional<Account> getAccountByPhone(final String Phone);

    Optional<Account> createAccount(Account account);

    Optional<String> getKakaoAuthorize(String code);
}
