package com.gospelee.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.enums.RedisCacheNames;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewLoginServiceTest {

  private static final String EMAIL = "review@privaterelay.appleid.com";

  private final Map<String, String> redis = new HashMap<>();
  private final AtomicLong now = new AtomicLong(1_000_000L);
  private RedisCacheService cache;

  @BeforeEach
  void setUp() {
    cache = mock(RedisCacheService.class);
    when(cache.put(any(RedisCacheDTO.class))).thenAnswer(inv -> {
      RedisCacheDTO dto = inv.getArgument(0);
      redis.put(dto.getRedisCacheNames() + "::" + dto.getKey(), (String) dto.getValue());
      return (String) dto.getValue();
    });
    when(cache.get(any(RedisCacheNames.class), any(String.class))).thenAnswer(
        inv -> redis.get(inv.getArgument(0) + "::" + inv.getArgument(1)));
  }

  private ReviewLoginService service(String id, String pw) {
    return new ReviewLoginService(cache, id, pw, EMAIL, now::get);
  }

  private ReviewLoginService service(String id, String pw, String accountEmail) {
    return new ReviewLoginService(cache, id, pw, accountEmail, now::get);
  }

  @Test
  void 올바른_ID와_비밀번호면_토큰을_발급하고_계정_이메일로_확인된다() {
    ReviewLoginService service = service("reviewer", "s3cret!");

    String token = service.login("reviewer", "s3cret!", "1.1.1.1").orElseThrow();

    assertThat(token).hasSizeGreaterThanOrEqualTo(40);
    assertThat(service.resolveEmail(token)).contains(EMAIL);
    assertThat(service.resolveEmail("other-token")).isEmpty();
  }

  @Test
  void 지정한_계정_이메일로_인증된다() {
    ReviewLoginService dedicated = service("reviewer", "pw", "dedicated@example.com");
    String token = dedicated.login("reviewer", "pw", "1.1.1.1").orElseThrow();

    assertThat(dedicated.resolveEmail(token)).contains("dedicated@example.com");
  }

  @Test
  void 발급할_때마다_다른_토큰이다() {
    ReviewLoginService service = service("reviewer", "pw");

    assertThat(service.login("reviewer", "pw", "1.1.1.1"))
        .isNotEqualTo(service.login("reviewer", "pw", "1.1.1.1"));
  }

  @Test
  void ID나_비밀번호가_틀리면_거부한다() {
    ReviewLoginService service = service("reviewer", "pw");

    assertThat(service.login("reviewer", "wrong", "1.1.1.1")).isEmpty();
    assertThat(service.login("wrong", "pw", "1.1.1.1")).isEmpty();
    assertThat(service.login(null, null, "1.1.1.1")).isEmpty();
  }

  @Test
  void 설정이_비어_있으면_비활성이다() {
    assertThat(service("", "").isEnabled()).isFalse();
    assertThat(service("", "").login("", "", "1.1.1.1")).isEmpty();

    // 계정 이메일이 없어도 비활성
    assertThat(service("reviewer", "pw", "").isEnabled()).isFalse();
  }

  @Test
  void 실패가_5번_반복되면_같은_IP는_올바른_비밀번호도_잠기고_시간이_지나면_풀린다() {
    ReviewLoginService service = service("reviewer", "pw");
    for (int i = 0; i < ReviewLoginService.MAX_FAILURES; i++) {
      service.login("reviewer", "bad", "9.9.9.9");
    }

    assertThat(service.login("reviewer", "pw", "9.9.9.9")).isEmpty();
    assertThat(service.login("reviewer", "pw", "2.2.2.2")).isPresent(); // 다른 IP 는 영향 없음

    now.addAndGet(ReviewLoginService.LOCK_MILLIS + 1);
    assertThat(service.login("reviewer", "pw", "9.9.9.9")).isPresent();
  }
}
