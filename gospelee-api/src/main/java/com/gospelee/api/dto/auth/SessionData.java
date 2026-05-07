package com.gospelee.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gospelee.api.enums.SocialLoginPlatform;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionData {

  private Long accountUid;

  private String email;

  private SocialLoginPlatform socialLoginPlatform;

  private String idToken;

  private String accessToken;

  private String refreshToken;

  private boolean superUser;

  public SessionData(Long accountUid, String email, SocialLoginPlatform socialLoginPlatform,
      String idToken, String accessToken, String refreshToken, boolean superUser) {
    this.accountUid = accountUid;
    this.email = email;
    this.socialLoginPlatform = socialLoginPlatform;
    this.idToken = idToken;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.superUser = superUser;
  }
}
