package com.gospelee.api.service;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountKakaoToken;
import com.gospelee.api.repository.AccountKakaoTokenRepository;
import com.gospelee.api.repository.AccountRepository;
import org.springframework.stereotype.Service;

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

    public Optional<Account> createAccount(Account account) {

        return accountRepository.findByPhone(account.getPhone()).map(acc -> {
            // 이미 계정이 존재하는 경우(핸드폰 번호가 존재함)
            List<AccountKakaoToken> tokenList = acc.getAccountKakaoTokenList();
            if (tokenList.isEmpty()) {
                // 토큰이 존재하지 않으면 새로운 토큰을 저장함
                AccountKakaoToken accountKakaoToken = AccountKakaoToken.builder()
                        .parentUid(acc.getUid())
                        .accessToken(account.getAccountKakaoToken().getAccessToken())
                        .accessTokenExpiresAt(account.getAccountKakaoToken().getAccessTokenExpiresAt())
                        .refreshToken(account.getAccountKakaoToken().getRefreshToken())
                        .refreshTokenExpiresAt(account.getAccountKakaoToken().getRefreshTokenExpiresAt())
                        .idToken(account.getAccountKakaoToken().getIdToken())
                        .deviceInfo(account.getAccountKakaoToken().getDeviceInfo())
                        .build();
                accountKakaoTokenRepository.save(accountKakaoToken);
            }

            return Optional.of(acc);

        }).orElseGet(() -> {
            // 계정이 존재하지 않는 경우 새로운 계정을 저장함
            Account savedAccount = accountRepository.save(account);
            if (savedAccount.getPhone() != null) {
                // 계정 최초 등록 성공 후 토큰 정보 저장
                AccountKakaoToken accountKakaoToken = AccountKakaoToken.builder()
                        .parentUid(savedAccount.getUid())
                        .accessToken(account.getAccountKakaoToken().getAccessToken())
                        .accessTokenExpiresAt(account.getAccountKakaoToken().getAccessTokenExpiresAt())
                        .refreshToken(account.getAccountKakaoToken().getRefreshToken())
                        .refreshTokenExpiresAt(account.getAccountKakaoToken().getRefreshTokenExpiresAt())
                        .idToken(account.getAccountKakaoToken().getIdToken())
                        .deviceInfo(account.getAccountKakaoToken().getDeviceInfo())
                        .build();
                accountKakaoTokenRepository.save(accountKakaoToken);
            } else {
                throw new RuntimeException("Account 저장 실패");
            }

            return Optional.of(savedAccount);

        });

    }

    public Optional<String> getKakaoAuthorize(String code) {
        Optional<String> result = Optional.empty();
        return result;
    }

}
