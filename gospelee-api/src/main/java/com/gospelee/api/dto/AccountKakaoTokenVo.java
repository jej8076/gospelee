package com.gospelee.api.dto;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountKakaoTokenVo {
	private String accessToken;

	private LocalDateTime accessExpiresAt;

	private String refreshToken;

	private LocalDateTime refreshTokenExpiresAt;

	private String idToken;

	private String deviceInfo;
}
