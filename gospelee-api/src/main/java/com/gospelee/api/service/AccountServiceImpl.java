package com.gospelee.api.service;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountKakaoToken;
import com.gospelee.api.repository.AccountKakaoTokenRepository;
import com.gospelee.api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final AccountKakaoTokenRepository accountKakaoTokenRepository;

    public AccountServiceImpl(AccountRepository accountRepository, AccountKakaoTokenRepository accountKakaoTokenRepository) {
        this.accountRepository = accountRepository;
        this.accountKakaoTokenRepository = accountKakaoTokenRepository;
    }

    @Override
    public Optional<Account> getAccountByPhone(String phone) {
        return accountRepository.findByPhone(phone);
    }

    public void createAccount(Account account) {
        int result = 0;
        accountRepository.findByPhone(account.getPhone()).ifPresentOrElse(acc -> {
//            throw new RuntimeException("중복됨ㅋ");

            List<AccountKakaoToken> tokenList = acc.getAccountKakaoTokenList();
            if (tokenList.size() < 1) {

                AccountKakaoToken accountKakaoToken = AccountKakaoToken.builder()
                        .parentUid(acc.getUid())
                        .accessToken(account.getAccountKakaoToken().getAccessToken())
                        .accessExpiresAt(account.getAccountKakaoToken().getAccessExpiresAt())
                        .refreshToken(account.getAccountKakaoToken().getRefreshToken())
                        .refreshTokenExpiresAt(account.getAccountKakaoToken().getRefreshTokenExpiresAt())
                        .idToken(account.getAccountKakaoToken().getIdToken())
                        .deviceInfo(account.getAccountKakaoToken().getDeviceInfo())
                        .build();
                accountKakaoTokenRepository.save(accountKakaoToken);
            }


        }, () -> {

            Account savedAccount = accountRepository.save(account);
            if (savedAccount != null && savedAccount.getPhone() != null) {
                // 계정 최초 등록 성공 로직

                AccountKakaoToken accountKakaoToken = AccountKakaoToken.builder()
                        .parentUid(savedAccount.getUid())
                        .accessToken(account.getAccountKakaoToken().getAccessToken())
                        .accessExpiresAt(account.getAccountKakaoToken().getAccessExpiresAt())
                        .refreshToken(account.getAccountKakaoToken().getRefreshToken())
                        .refreshTokenExpiresAt(account.getAccountKakaoToken().getRefreshTokenExpiresAt())
                        .idToken(account.getAccountKakaoToken().getIdToken())
                        .deviceInfo(account.getAccountKakaoToken().getDeviceInfo())
                        .build();

                accountKakaoTokenRepository.save(accountKakaoToken);
            } else {
                throw new RuntimeException("Account 저장 실패");
            }

        });

    }

    public Optional<String> getKakaoAuthorize(String code) {
        Optional<String> result = Optional.empty();
        return result;
    }

}
