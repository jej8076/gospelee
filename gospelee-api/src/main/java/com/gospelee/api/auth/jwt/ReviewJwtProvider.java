package com.gospelee.api.auth.jwt;

import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.enums.SocialLoginPlatform;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.ReviewLoginService;
import org.springframework.stereotype.Component;

/**
 * 앱스토어 심사용 계정 인증. 소셜 id token 이 아니라 {@link ReviewLoginService} 가 발급한 토큰을 확인한다.
 */
@Component
public class ReviewJwtProvider extends SocialJwtProvider {

  private static final String ISSUER = "podo-review";

  private final ReviewLoginService reviewLoginService;

  public ReviewJwtProvider(AccountService accountService, ReviewLoginService reviewLoginService) {
    super(accountService);
    this.reviewLoginService = reviewLoginService;
  }

  @Override
  public SocialLoginPlatform getSupportedPlatform() {
    return SocialLoginPlatform.REVIEW;
  }

  @Override
  public JwtPayload getOIDCPayload(String token, String nonceCacheKey) {
    return reviewLoginService.resolveEmail(token)
        .map(email -> JwtPayload.builder()
            .issuer(ISSUER)
            .sub(email)
            .email(email)
            .nickname("앱 심사용 계정")
            .build())
        .orElse(null);
  }
}
