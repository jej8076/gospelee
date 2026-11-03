package com.gospelee.api.dto.pushdirect;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 공지·주보 등록 없이 직접 푸시를 보내는 요청. groupIds 가 비어 있으면 교회 성도 전체에게 보낸다 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushDirectRequestDTO {

  @NotBlank
  @Size(max = 50)
  private String title;
  @NotBlank
  @Size(max = 200)
  private String message;
  private List<Long> groupIds;

  @Builder
  public PushDirectRequestDTO(String title, String message, List<Long> groupIds) {
    this.title = title;
    this.message = message;
    this.groupIds = groupIds;
  }
}
