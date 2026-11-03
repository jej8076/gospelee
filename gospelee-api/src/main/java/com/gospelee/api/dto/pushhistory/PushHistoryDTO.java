package com.gospelee.api.dto.pushhistory;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** 푸시 발송 이력 한 건과 상태별 인원 */
@Getter
@Builder
public class PushHistoryDTO {

  private final Long pushId;
  private final String title;
  private final String message;
  private final String category;
  private final String senderName;
  private final LocalDateTime sentTime;
  /** 토큰이 있어 FCM 에 보낸 대상 수 */
  private final int targetCount;
  /** FCM 이 접수한 수 (받음·확인 포함) */
  private final int deliveredCount;
  private final int failedCount;
  /** 앱이 받았다고 알려 온 수 (확인 포함) */
  private final int receivedCount;
  private final int confirmedCount;
}
