package com.gospelee.api.service;

import com.gospelee.api.entity.Account;
import com.gospelee.api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Account> getAccount(String slaveId) {
        return accountRepository.findById(slaveId);
    }

    public void createAccount(Account account) {
        // TODO phone 컬럼에 대한 중복체크를 하던지 아니면 primary key 로 만든다
        accountRepository.save(account);
    }

}
