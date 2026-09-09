package com.gospelee.api.dto.biblereading;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingJoinRequestDTO {

  @NotEmpty(message = "초대 코드는 필수입니다.")
  private String inviteCode;
}
