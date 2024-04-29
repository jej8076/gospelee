package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.RoleType;
import com.gospelee.api.repository.AccountKakaoTokenRepository;
import com.gospelee.api.repository.AccountRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;

  private final AccountKakaoTokenRepository accountKakaoTokenRepository;

  public AccountServiceImpl(AccountRepository accountRepository,
      AccountKakaoTokenRepository accountKakaoTokenRepository) {
    this.accountRepository = accountRepository;
    this.accountKakaoTokenRepository = accountKakaoTokenRepository;
  }

  public List<Account> getAccountAll() {
    return accountRepository.findAll();
  }

  @Override
  public Optional<Account> getAccountByPhone(String phone) {
    return accountRepository.findByPhone(phone);
  }

  /**
   * token값으로 account 정보를 가져온다
   *
   * @param token
   * @return
   */
  public Optional<Account> getAccountByToken(String token) {
    return accountKakaoTokenRepository.findByAccessToken(token)
        .map(result -> accountRepository.findById(String.valueOf(result.getParentUid())))
        .orElseThrow(
            () -> new NoSuchElementException("No account found with the given token: " + token));
  }

  public Optional<Account> saveAndGetAccount(JwtPayload jwtPayload) {
    return accountRepository.findByEmail(jwtPayload.getEmail())
        .map(Optional::of).orElseGet(() -> {
          Account account = Account.builder()
              .name(jwtPayload.getNickname())
              .email(jwtPayload.getEmail())
              .role(RoleType.LAYMAN)
              .build();
          // 계정이 존재하지 않는 경우 새로운 계정을 저장함
          Account savedAccount = accountRepository.save(account);

          if (ObjectUtils.isEmpty(account)) {
            throw new RuntimeException("Account 저장 실패");
          }

          return Optional.of(savedAccount);
        });
  }

}


