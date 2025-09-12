package com.gospelee.api.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 모르는 필드는 무시
@JsonIgnoreProperties(ignoreUnknown = true)
// camelCase 적용
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

public class ProfileResponse {

  private String nickName;
  private String profileImageUrl;

}
