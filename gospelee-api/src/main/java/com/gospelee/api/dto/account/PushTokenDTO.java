package com.gospelee.api.dto.account;

import lombok.Getter;

@Getter
public class PushTokenDTO {

  private String pushToken;

  // 애플 로그인 직후에만 전달되는 인증 코드 (refresh token 교환용)
  private String authorizationCode;
}
