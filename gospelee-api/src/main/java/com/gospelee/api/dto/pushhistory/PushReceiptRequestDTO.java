package com.gospelee.api.dto.pushhistory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 앱이 푸시를 받았거나(RECEIVED) 눌러서 열었다(CONFIRMED)고 알리는 요청 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushReceiptRequestDTO {

  @NotNull
  private Long pushId;

  /** RECEIVED 또는 CONFIRMED */
  @NotNull
  private String status;
}
