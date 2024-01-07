package com.gospelee.api.service;

import com.gospelee.api.entity.Account;
import com.gospelee.api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Account> getAccountByPhone(String phone) {
        return accountRepository.findByPhone(phone);
    }

    public void createAccount(Account account) {
        accountRepository.findByPhone(account.getPhone()).ifPresentOrElse(acc -> {
            throw new RuntimeException("중복됨ㅋ");
        }, () -> accountRepository.save(account));
    }

    public Optional<String> getKakaoAuthorize(String code) {
        Optional<String> result = Optional.empty();
        return result;
    }

}
