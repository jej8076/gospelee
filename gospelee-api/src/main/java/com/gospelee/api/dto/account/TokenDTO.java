package com.gospelee.api.dto.account;

import static com.google.auth.http.AuthHttpConstants.BEARER;

import com.gospelee.api.enums.SocialLoginType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenDTO {

  private SocialLoginType socialLoginType;
  private String idToken;
  private String accessToken;
  private String refreshToken;

  @Builder
  public TokenDTO(SocialLoginType socialLoginType, String idToken, String accessToken,
      String refreshToken) {
    this.socialLoginType = socialLoginType;
    this.idToken = idToken;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  public void removeBearerIdToken() {
    this.idToken = idToken.replace(BEARER + " ", "");
  }
}
