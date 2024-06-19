package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.RoleType;
import com.gospelee.api.repository.AccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;

  public AccountServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public List<Account> getAccountAll() {
    return accountRepository.findAll();
  }

  public Optional<List<Account>> getAccountByEcclesiaUid(String ecclesiaUid) {
    return accountRepository.findByEcclesiaUid(ecclesiaUid);
  }

  @Override
  public Optional<Account> getAccountByPhone(String phone) {
    return accountRepository.findByPhone(phone);
  }

  /**
   * email로 account정보를 가져온다
   *
   * @param email
   * @return
   */
  public Optional<Account> getAccountByEmail(String email) {
    return accountRepository.findByEmail(email);
  }

  public Optional<Account> saveAndGetAccount(JwtPayload jwtPayload, String idToken) {
    return accountRepository.findByEmail(jwtPayload.getEmail())
        .map(acc -> Optional.of(
            accountRepository.updateAccountIdTokenAndFindById(acc.getUid(), idToken)))
        .orElseGet(() -> {
          Account account = Account.builder()
              .name(jwtPayload.getNickname())
              .email(jwtPayload.getEmail())
              .role(RoleType.LAYMAN)
              .id_token(idToken)
              .build();
          // 계정이 존재하지 않는 경우 새로운 계정을 저장함
          Account savedAccount = accountRepository.save(account);

          if (ObjectUtils.isEmpty(savedAccount)) {
            throw new RuntimeException("Account 저장 실패");
          }

          return Optional.of(savedAccount);
        });
  }

  public void savePushToken(Long uid, String pushToken) {
    accountRepository.savePushToken(uid, pushToken, LocalDateTime.now());
  }

}


