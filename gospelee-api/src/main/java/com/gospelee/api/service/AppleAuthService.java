package com.gospelee.api.service;

import com.gospelee.api.auth.jwt.AppleJwtProvider;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AppleAuthToken;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.account.AppleAuthTokenRepository;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 애플 id token(약 10분) 갱신.
 * 로그인 때 받은 authorizationCode 로 refresh token 을 받아 저장해 두고,
 * 앱이 만료된 id token 으로 갱신을 요청하면 저장된 refresh token 으로 새 id token 을 내려준다.
 */
@Slf4j
@Service
public class AppleAuthService {

  private final AppleAuthClient appleAuthClient;
  private final AppleJwtProvider appleJwtProvider;
  private final AppleAuthTokenRepository tokenRepository;
  private final AccountRepository accountRepository;
  private final Duration expiredGrace;

  public AppleAuthService(AppleAuthClient appleAuthClient, AppleJwtProvider appleJwtProvider,
      AppleAuthTokenRepository tokenRepository, AccountRepository accountRepository,
      @Value("${apple.refresh-grace-days:60}") long graceDays) {
    this.appleAuthClient = appleAuthClient;
    this.appleJwtProvider = appleJwtProvider;
    this.tokenRepository = tokenRepository;
    this.accountRepository = accountRepository;
    this.expiredGrace = Duration.ofDays(graceDays);
  }

  /** 로그인 직후 호출. 실패해도 로그인 자체에는 영향이 없도록 예외를 던지지 않는다. */
  @Transactional
  public void saveRefreshToken(Long accountUid, String authorizationCode) {
    if (accountUid == null || !StringUtils.hasText(authorizationCode)) {
      return;
    }
    try {
      appleAuthClient.exchangeAuthorizationCode(authorizationCode)
          .map(AppleAuthClient.AppleTokenResponse::getRefreshToken)
          .filter(StringUtils::hasText)
          .ifPresentOrElse(refreshToken -> {
            AppleAuthToken token = tokenRepository.findById(accountUid)
                .orElseGet(() -> new AppleAuthToken(accountUid, refreshToken));
            token.changeRefreshToken(refreshToken);
            tokenRepository.save(token);
            log.info("[APPLE] refresh token 저장 accountUid:{}", accountUid);
          }, () -> log.warn("[APPLE] refresh token 을 받지 못했습니다. accountUid:{}", accountUid));
    } catch (Exception e) {
      log.warn("[APPLE] refresh token 저장 실패 accountUid:{}: {}", accountUid, e.getMessage());
    }
  }

  /**
   * 만료된 id token 으로 새 id token 을 발급받는다.
   * 서명·발급자·앱 대상이 맞고, 만료된 지 apple.refresh-grace-days(기본 60일) 이내여야 한다.
   *
   * @return 새 id token, 갱신할 수 없으면 empty
   */
  public Optional<String> refreshIdToken(String expiredIdToken) {
    if (!StringUtils.hasText(expiredIdToken)) {
      return Optional.empty();
    }
    Optional<Claims> claimsOpt = appleJwtProvider.verifyIgnoringExpiry(expiredIdToken);
    if (claimsOpt.isEmpty() || !isAcceptable(claimsOpt.get())) {
      return Optional.empty();
    }

    String email = claimsOpt.get().get("email", String.class);
    Optional<Account> account = email == null ? Optional.empty()
        : accountRepository.findByEmail(email);
    if (account.isEmpty() || account.get().getLeaveYn() == Yn.Y) {
      return Optional.empty();
    }

    Optional<AppleAuthToken> stored = tokenRepository.findById(account.get().getUid());
    if (stored.isEmpty()) {
      log.info("[APPLE] 저장된 refresh token 없음 accountUid:{}", account.get().getUid());
      return Optional.empty();
    }

    return appleAuthClient.refresh(stored.get().getRefreshToken())
        .map(AppleAuthClient.AppleTokenResponse::getIdToken)
        .filter(StringUtils::hasText);
  }

  private boolean isAcceptable(Claims claims) {
    if (!appleJwtProvider.getIssuer().equals(claims.getIssuer())) {
      return false;
    }
    if (claims.getAudience() == null || !claims.getAudience().contains(appleJwtProvider.getAppKey())) {
      return false;
    }
    if (claims.getExpiration() == null) {
      return false;
    }
    long expiredMillis = System.currentTimeMillis() - claims.getExpiration().getTime();
    return expiredMillis <= expiredGrace.toMillis();
  }
}
