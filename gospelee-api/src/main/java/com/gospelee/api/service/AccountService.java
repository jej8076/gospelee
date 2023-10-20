package com.gospelee.api.service;

import com.gospelee.api.entity.Account;

import java.util.Optional;

public interface AccountService {
    Optional<Account> getAccount(final String id);

    void createAccount(Account account);
}
