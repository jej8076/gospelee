package com.gospelee.api.dto.account;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountKakaoTokenDTO {

  private String accessToken;

  private LocalDateTime accessTokenExpiresAt;

  private String refreshToken;

  private LocalDateTime refreshTokenExpiresAt;

  private String idToken;

  private String deviceInfo;
}
