package com.gospelee.api.dto.pushdirect;

import lombok.Builder;
import lombok.Getter;

/** 직접 푸시 발송 결과. (code/message 필드가 없어 오류 응답과 구분된다) */
@Getter
@Builder
public class PushDirectResponseDTO {

  private final Long pushId;
  private final int targetCount;
  private final int sentCount;
  private final int failedCount;
  private final int noTokenCount;
}
