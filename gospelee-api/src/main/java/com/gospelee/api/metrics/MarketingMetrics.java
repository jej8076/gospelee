package com.gospelee.api.metrics;

import com.gospelee.api.enums.SocialLoginPlatform;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Set;
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

  /** 성경 읽기 방식. 앱이 보낸 값 중 이 목록에 없는 값은 normal 로 센다(태그 값이 마구 늘지 않게) */
  private static final Set<String> BIBLE_VIEW_MODES = Set.of("normal", "swipe");

  private final MeterRegistry registry;

  public MarketingMetrics(MeterRegistry registry) {
    this.registry = registry;
    for (SocialLoginPlatform platform : SocialLoginPlatform.values()) {
      signupCounter(platform);
    }
    for (String mode : BIBLE_VIEW_MODES) {
      bibleViewCounter(mode);
    }
  }

  /** 신규 회원가입 1건 (로그인 수단별) */
  public void signup(SocialLoginPlatform platform) {
    signupCounter(platform == null ? SocialLoginPlatform.EMPTY : platform).increment();
  }

  /**
   * 성경 한 장을 불러옴 (읽기 방식별). 앱이 다음 장을 미리 받아 두는 요청도 포함한다.
   * mode: normal(그냥 읽기, 값 없음 포함) · swipe(스와이프하여 읽기)
   */
  public void bibleView(String mode) {
    bibleViewCounter(mode != null && BIBLE_VIEW_MODES.contains(mode) ? mode : "normal").increment();
  }

  private Counter bibleViewCounter(String mode) {
    return Counter.builder(PREFIX + "bible.view")
        .description("성경 장 불러오기 수(읽기 방식별)")
        .tag("mode", mode)
        .register(registry);
  }

  private Counter signupCounter(SocialLoginPlatform platform) {
    return Counter.builder(PREFIX + "signup")
        .description("신규 회원가입 수")
        .tag("platform", platform.name())
        .register(registry);
  }
}
