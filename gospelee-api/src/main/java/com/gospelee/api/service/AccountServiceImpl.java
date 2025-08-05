package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.ecclesia.AccountEcclesiaHistoryDetailDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.exception.EcclesiaException;
import com.gospelee.api.repository.AccountEcclesiaHistoryRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.ecclesia.EcclesiaJpaRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * 계정 관련 비즈니스 로직을 처리하는 서비스 구현체 - 계정 조회, 생성, 업데이트 등의 기능을 제공 - JWT 토큰 기반 인증 처리 - 교회 정보와 연동된 계정 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private static final String SUPER_TOKEN = "SUPER";
  private static final String SUPER_EMAIL = "super@super.com";

  private final AccountRepository accountRepository;
  private final AccountEcclesiaHistoryRepository accountEcclesiaHistoryRepository;
  private final EcclesiaJpaRepository ecclesiaJpaRepository;

  /**
   * 모든 계정을 조회합니다.
   *
   * @return 전체 계정 목록
   */
  @Override
  public List<Account> getAccountAll() {
    log.debug("전체 계정 목록 조회 요청");
    return accountRepository.findAll();
  }

  /**
   * 교회 UID로 해당 교회의 계정 목록을 조회합니다.
   *
   * @param ecclesiaUid 교회 UID
   * @return 교회별 계정 목록
   */
  @Override
  public Optional<List<Account>> getAccountByEcclesiaUid(Long ecclesiaUid) {
    log.debug("교회별 계정 목록 조회 요청. ecclesiaUid: {}", ecclesiaUid);
    return accountRepository.findByEcclesiaUid(ecclesiaUid);
  }

  @Override
  public List<AccountEcclesiaHistoryDetailDTO> getAccountEcclesiaRequestList() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (account.getEcclesiaUid() == null) {
      throw new EcclesiaException("[ACCOUNT   ] not_affiliated_with_church accountUid:{}",
          account.getUid());
    }

    return accountEcclesiaHistoryRepository.findByAccountEcclesiaRequestByEcclesiaUid(
        account.getEcclesiaUid());
  }

  /**
   * 전화번호로 계정을 조회합니다.
   *
   * @param phone 전화번호
   * @return 계정 정보
   */
  @Override
  public Optional<Account> getAccountByPhone(String phone) {
    log.debug("전화번호로 계정 조회 요청. phone: {}", phone);
    return accountRepository.findByPhone(phone);
  }

  /**
   * 이메일로 계정을 조회합니다.
   *
   * @param email 이메일 주소
   * @return 계정 정보
   */
  @Override
  public Optional<Account> getAccountByEmail(String email) {
    log.debug("이메일로 계정 조회 요청. email: {}", email);
    return accountRepository.findByEmail(email);
  }

  // ========== 계정 생성 및 업데이트 관련 메서드 ==========

  /**
   * JWT 페이로드를 기반으로 계정을 저장하거나 업데이트하고 인증 정보를 반환합니다.
   *
   * @param jwtPayload JWT 페이로드 정보
   * @param idToken    ID 토큰
   * @return 인증된 계정 정보
   */
  @Override
  public Optional<AccountAuthDTO> saveAndGetAccount(JwtPayload jwtPayload, String idToken) {
    log.debug("계정 저장/업데이트 요청. email: {}, idToken: {}", jwtPayload.getEmail(), idToken);

    if (isSuperUserToken(idToken)) {
      return handleSuperUserAuthentication();
    }

    Account account = findOrCreateAccount(jwtPayload, idToken);
    return buildAccountAuthDTO(account);
  }

  /**
   * 계정의 푸시 토큰을 저장합니다.
   *
   * @param uid       계정 UID
   * @param pushToken 푸시 토큰
   */
  @Override
  public void savePushToken(Long uid, String pushToken) {
    log.debug("푸시 토큰 저장 요청. uid: {}", uid);
    accountRepository.savePushToken(uid, pushToken, LocalDateTime.now());
  }

  // ========== Private Helper Methods ==========

  /**
   * 슈퍼 유저 토큰인지 확인합니다.
   */
  private boolean isSuperUserToken(String idToken) {
    return SUPER_TOKEN.equals(idToken);
  }

  /**
   * 슈퍼 유저 인증을 처리합니다.
   */
  private Optional<AccountAuthDTO> handleSuperUserAuthentication() {
    log.debug("슈퍼 유저 인증 처리");

    Account superAccount = accountRepository.findByEmail(SUPER_EMAIL)
        .orElseThrow(() -> new RuntimeException("슈퍼 계정을 찾을 수 없습니다."));

    Optional<Ecclesia> ecclesia = ecclesiaJpaRepository.findEcclesiasByMasterAccountUid(
        superAccount.getUid());

    AccountAuthDTO authDTO = AccountAuthDTO.builder()
        .uid(superAccount.getUid())
        .email(superAccount.getEmail())
        .name(superAccount.getName())
        .phone(superAccount.getPhone())
        .rrn(superAccount.getRrn())
        .role(RoleType.ADMIN)
        .ecclesiaUid(ecclesia.map(Ecclesia::getUid).orElse(null))
        .ecclesiaStatus(ecclesia.map(Ecclesia::getStatus).orElse(null))
        .build();

    return Optional.of(authDTO);
  }

  /**
   * 기존 계정을 찾거나 새로운 계정을 생성합니다.
   */
  private Account findOrCreateAccount(JwtPayload jwtPayload, String idToken) {
    return accountRepository.findByEmail(jwtPayload.getEmail())
        .map(existingAccount -> updateExistingAccount(existingAccount, idToken))
        .orElseGet(() -> createNewAccount(jwtPayload, idToken));
  }

  /**
   * 기존 계정의 ID 토큰을 업데이트합니다.
   */
  private Account updateExistingAccount(Account existingAccount, String idToken) {
    log.debug("기존 계정 ID 토큰 업데이트. uid: {}", existingAccount.getUid());
    return accountRepository.updateAccountIdTokenAndFindById(existingAccount.getUid(), idToken);
  }

  /**
   * 새로운 계정을 생성합니다.
   */
  private Account createNewAccount(JwtPayload jwtPayload, String idToken) {
    log.debug("새로운 계정 생성. email: {}", jwtPayload.getEmail());

    Account newAccount = Account.builder()
        .name(jwtPayload.getNickname())
        .email(jwtPayload.getEmail())
        .role(RoleType.LAYMAN)
        .idToken(idToken)
        .build();

    Account savedAccount = accountRepository.save(newAccount);

    if (ObjectUtils.isEmpty(savedAccount)) {
      throw new RuntimeException("계정 저장에 실패했습니다.");
    }

    return savedAccount;
  }

  /**
   * 계정 정보를 기반으로 인증 DTO를 생성합니다.
   */
  private Optional<AccountAuthDTO> buildAccountAuthDTO(Account account) {
    log.debug("계정 인증 DTO 생성. uid: {}", account.getUid());

    Optional<Ecclesia> ecclesia = ecclesiaJpaRepository.findEcclesiasByMasterAccountUid(
        account.getUid());

    AccountAuthDTO authDTO = AccountAuthDTO.builder()
        .uid(account.getUid())
        .email(account.getEmail())
        .name(account.getName())
        .phone(account.getPhone())
        .rrn(account.getRrn())
        .role(account.getRole())
        .idToken(account.getIdToken())
        .pushToken(account.getPushToken())
        .ecclesiaUid(ecclesia.map(Ecclesia::getUid).orElse(null))
        .ecclesiaStatus(ecclesia.map(Ecclesia::getStatus).orElse(null))
        .build();

    return Optional.of(authDTO);
  }
}


