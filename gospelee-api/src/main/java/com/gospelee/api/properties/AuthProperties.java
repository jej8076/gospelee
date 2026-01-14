package com.gospelee.api.properties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "auth")
@Component
@Getter
@Setter
public class AuthProperties {

  private String superId;
  private String superPass;
  private List<String> excludePaths;
  private List<String> allowPendingPaths;
  private String appStoreSkipEmail;

  /**
   * nonce 검증을 건너뛸 이메일인지 확인합니다. 앱스토어 심사용 계정 등에 사용됩니다.
   */
  public boolean shouldSkipNonceValidation(String email) {
    if (appStoreSkipEmail == null || email == null) {
      return false;
    }
    return email.equals(appStoreSkipEmail);
  }
}
