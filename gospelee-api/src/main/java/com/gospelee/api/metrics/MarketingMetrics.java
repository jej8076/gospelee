package com.gospelee.api.metrics;

import com.gospelee.api.enums.SocialLoginPlatform;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * 마케팅 지표. API 요청 수로는 구분할 수 없는 사건(예: 로그인과 같은 API 를 쓰는 회원가입)을 직접 센다.
 * <p>
 * 이름은 모두 podo.marketing.* 로 시작하고, Prometheus 에서는 podo_marketing_*_total 로 보인다.
 * Grafana 마케팅 대시보드의 "마케팅 이벤트" 칸이 이 접두어를 자동으로 그리므로, 새 지표는 여기에 메서드만 추가하면 된다.
 * 처음 생긴 카운터는 첫 증가분이 increase() 에 잡히지 않으므로, 쓸 태그 조합은 생성자에서 0 으로 미리 만든다.
 */
@Component
public class MarketingMetrics {

  private static final String PREFIX = "podo.marketing.";

  private final MeterRegistry registry;

  public MarketingMetrics(MeterRegistry registry) {
    this.registry = registry;
    for (SocialLoginPlatform platform : SocialLoginPlatform.values()) {
      signupCounter(platform);
    }
  }

  /** 신규 회원가입 1건 (로그인 수단별) */
  public void signup(SocialLoginPlatform platform) {
    signupCounter(platform == null ? SocialLoginPlatform.EMPTY : platform).increment();
  }

  private Counter signupCounter(SocialLoginPlatform platform) {
    return Counter.builder(PREFIX + "signup")
        .description("신규 회원가입 수")
        .tag("platform", platform.name())
        .register(registry);
  }
}
