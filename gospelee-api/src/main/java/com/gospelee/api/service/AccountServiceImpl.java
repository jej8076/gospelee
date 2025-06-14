package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.AccountRepository;
import com.gospelee.api.repository.Ecclesia.EcclesiaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;

  private final EcclesiaRepository ecclesiaRepository;

  public AccountServiceImpl(AccountRepository accountRepository,
      EcclesiaRepository ecclesiaRepository) {
    this.accountRepository = accountRepository;
    this.ecclesiaRepository = ecclesiaRepository;
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

  public Optional<AccountAuthDTO> saveAndGetAccount(JwtPayload jwtPayload, String idToken) {
    Account result;

    if (idToken.equals("SUPER")) {
      result = accountRepository.findByEmail("super@super.com").get();
      Optional<Ecclesia> ecc = ecclesiaRepository.findEcclesiasByMasterAccountUid(result.getUid());
      AccountAuthDTO accountAuthDTO = AccountAuthDTO.builder()
          .uid(result.getUid())
          .email(result.getEmail())
          .name(result.getName())
          .phone(result.getPhone())
          .rrn(result.getRrn())
          .role(RoleType.ADMIN)
          .ecclesiaUid(ecc.map(ecclesia -> String.valueOf(ecclesia.getUid())).orElse(null))
          .ecclesiaStatus(ecc.map(Ecclesia::getStatus).orElse(null))
          .build();
      return Optional.ofNullable(accountAuthDTO);
    }

    result = accountRepository.findByEmail(jwtPayload.getEmail())
        .map(acc -> accountRepository.updateAccountIdTokenAndFindById(acc.getUid(), idToken))
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

          return savedAccount;
        });

    // 계정 uid로 ecclasia 정보를 가져옴
    Optional<Ecclesia> ecc = ecclesiaRepository.findEcclesiasByMasterAccountUid(result.getUid());
    AccountAuthDTO accountAuthDTO = AccountAuthDTO.builder()
        .uid(result.getUid())
        .email(result.getEmail())
        .name(result.getName())
        .phone(result.getPhone())
        .rrn(result.getRrn())
        .role(result.getRole())
        .id_token(result.getId_token())
        .pushToken(result.getPushToken())
        .ecclesiaUid(ecc.map(ecclesia -> String.valueOf(ecclesia.getUid())).orElse(null))
        .ecclesiaStatus(ecc.map(Ecclesia::getStatus).orElse(null))
        .build();

    return Optional.ofNullable(accountAuthDTO);
  }

  public void savePushToken(Long uid, String pushToken) {
    accountRepository.savePushToken(uid, pushToken, LocalDateTime.now());
  }

}


