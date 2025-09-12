package com.gospelee.api.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.api.client.util.DateTime;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserMeResponse {

  private long id;
  private boolean has_signed_up;
  private KakaoAccount kakaoAccount;
  private LocalDateTime connectedAt;
  private LocalDateTime synchedAt;
  @JsonIgnore
  private String properties;
  private String forPartner;

}
