package com.gospelee.api.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "session.cookie")
public class SessionCookieProperties {

  /** Cookie 이름. 기본 SESSION. */
  private String name = "SESSION";

  /** Cookie Domain. 비어 있으면 미설정 (정확한 호스트만 매칭). 운영은 ".po-do.org". */
  private String domain = "";

  /** Cookie Path. */
  private String path = "/";

  /** Secure 플래그. 운영은 true (HTTPS 필수), 로컬은 false. */
  private boolean secure = false;

  /** SameSite 정책. Lax 권장. */
  private String sameSite = "Lax";

  /** Max-Age (초). 7일 = 604800. */
  private long maxAge = 604800;
}
