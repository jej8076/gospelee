package com.gospelee.api.dto.kakao;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMeResponse {

  private long id;
  private KakaoAccount kakaoAccount;
  private String forPartner;
  private Instant connectedAt;
  private Instant synchedAt;

}
