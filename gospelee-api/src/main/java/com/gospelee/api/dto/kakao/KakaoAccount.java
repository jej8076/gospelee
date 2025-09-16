package com.gospelee.api.dto.kakao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoAccount {

  private ProfileResponse profile;
  private String email;
  private String phoneNumber;

}
