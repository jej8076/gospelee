package com.gospelee.api.dto.bulletin;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 주보 등록/수정 요청 (multipart 의 body 파트) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BulletinRequestDTO {

  /** 수정 시에만 사용 */
  private Long id;
  @NotNull
  private LocalDate worshipDate;
  private String title;
  /** Y/N, 비우면 등록은 Y, 수정은 변경 없음 */
  private String openYn;
  /** 수정 시 삭제할 file_details ID 목록 */
  private List<Long> deleteFileDetailIdList;
  /** Y 이면 등록 후 푸시 알림을 보낸다. 비우면 보내지 않는다 */
  private String pushNotificationSendYn;
  /** 알림을 보낼 푸시 그룹 id. 비어 있으면 교회 성도 전체 */
  private List<Long> pushGroupIds;

  @Builder
  public BulletinRequestDTO(Long id, LocalDate worshipDate, String title, String openYn,
      List<Long> deleteFileDetailIdList, String pushNotificationSendYn,
      List<Long> pushGroupIds) {
    this.id = id;
    this.worshipDate = worshipDate;
    this.title = title;
    this.openYn = openYn;
    this.deleteFileDetailIdList = deleteFileDetailIdList;
    this.pushNotificationSendYn = pushNotificationSendYn;
    this.pushGroupIds = pushGroupIds;
  }
}
