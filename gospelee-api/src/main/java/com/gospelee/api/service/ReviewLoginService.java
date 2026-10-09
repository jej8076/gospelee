package com.gospelee.api.service;

import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.enums.RedisCacheNames;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 앱스토어 심사용 계정 로그인.
 * 애플 id token(약 10분)을 재사용하던 방식 대신, 서버가 설정된 ID/비밀번호를 직접 확인하고
 * 자체 토큰(Redis, 30일)을 발급한다. 인증되는 계정은 review.account-email 이다.
 * review.login-id / review.login-password / review.account-email 중 하나라도 비어 있으면 기능이 꺼진다.
 */
@Slf4j
@Service
public class ReviewLoginService {

  static final int MAX_FAILURES = 5;
  static final long LOCK_MILLIS = 10 * 60 * 1000L;

  private final RedisCacheService redisCacheService;
  private final String loginId;
  private final String loginPassword;
  private final String accountEmail;
  private final LongSupplier clock;
  private final SecureRandom random = new SecureRandom();
  private final Map<String, Failure> failures = new ConcurrentHashMap<>();

  @Autowired
  public ReviewLoginService(RedisCacheService redisCacheService,
      @Value("${review.login-id:}") String loginId,
      @Value("${review.login-password:}") String loginPassword,
      @Value("${review.account-email:}") String accountEmail) {
    this(redisCacheService, loginId, loginPassword, accountEmail,
        System::currentTimeMillis);
  }

  ReviewLoginService(RedisCacheService redisCacheService,
      String loginId, String loginPassword, String accountEmail, LongSupplier clock) {
    this.redisCacheService = redisCacheService;
    this.loginId = loginId == null ? "" : loginId;
    this.loginPassword = loginPassword == null ? "" : loginPassword;
    this.accountEmail = accountEmail == null ? "" : accountEmail.trim();
    this.clock = clock;
  }

  public boolean isEnabled() {
    return StringUtils.hasText(loginId) && StringUtils.hasText(loginPassword)
        && StringUtils.hasText(accountEmail);
  }

  /**
   * ID/비밀번호가 맞으면 토큰을 발급한다. 같은 IP 에서 실패가 반복되면 일정 시간 잠근다.
   *
   * @return 발급된 토큰, 실패(비활성·잠김·불일치)하면 empty
   */
  public Optional<String> login(String id, String password, String clientIp) {
    if (!isEnabled() || isLocked(clientIp)) {
      return Optional.empty();
    }
    // 두 값을 모두 비교한 뒤 판단해 어느 쪽이 틀렸는지 시간차로 드러나지 않게 한다
    boolean idMatches = constantTimeEquals(loginId, id);
    boolean passwordMatches = constantTimeEquals(loginPassword, password);
    if (!(idMatches && passwordMatches)) {
      recordFailure(clientIp);
      log.warn("[REVIEW-LOGIN] 로그인 실패 ip:{}", clientIp);
      return Optional.empty();
    }

    failures.remove(clientIp);
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    redisCacheService.put(RedisCacheDTO.builder()
        .redisCacheNames(RedisCacheNames.REVIEW_LOGIN)
        .key(token)
        .value(accountEmail)
        .build());
    log.info("[REVIEW-LOGIN] 로그인 성공 ip:{}", clientIp);
    return Optional.of(token);
  }

  /** 발급한 토큰에 해당하는 심사용 계정 이메일. 없거나 만료됐으면 empty */
  public Optional<String> resolveEmail(String token) {
    if (!isEnabled() || !StringUtils.hasText(token)) {
      return Optional.empty();
    }
    return Optional.ofNullable(redisCacheService.get(RedisCacheNames.REVIEW_LOGIN, token))
        .filter(StringUtils::hasText);
  }

  private boolean isLocked(String ip) {
    Failure f = failures.get(ip);
    return f != null && f.count >= MAX_FAILURES && clock.getAsLong() - f.lastAt < LOCK_MILLIS;
  }

  private void recordFailure(String ip) {
    long now = clock.getAsLong();
    failures.merge(ip, new Failure(1, now), (old, ignored) ->
        now - old.lastAt >= LOCK_MILLIS ? new Failure(1, now) : new Failure(old.count + 1, now));
  }

  private static boolean constantTimeEquals(String expected, String actual) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] a = digest.digest(expected.getBytes(StandardCharsets.UTF_8));
      byte[] b = digest.digest((actual == null ? "" : actual).getBytes(StandardCharsets.UTF_8));
      return MessageDigest.isEqual(a, b);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private record Failure(int count, long lastAt) {

  }
}
