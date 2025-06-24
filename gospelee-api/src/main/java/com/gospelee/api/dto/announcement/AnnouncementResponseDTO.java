package com.gospelee.api.dto.announcement;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnouncementResponseDTO {

  private Long id;
  @NotBlank
  private String organizationType;
  private Long organizationId;
  private String organizationName;
  @NotBlank
  private String subject;
  private String text;
  private Long fileUid;
  @NotBlank
  private String pushNotificationSendYn;
  private String pushNotificationIds;
  private LocalDateTime insertTime;
  private LocalDateTime updateTime;

  @Builder
  @QueryProjection
  public AnnouncementResponseDTO(Long id, String organizationType, Long organizationId,
      String organizationName, String subject, String text, Long fileUid,
      String pushNotificationSendYn, String pushNotificationIds, LocalDateTime insertTime,
      LocalDateTime updateTime) {
    this.id = id;
    this.organizationType = organizationType;
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.pushNotificationSendYn = pushNotificationSendYn;
    this.pushNotificationIds = pushNotificationIds;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
  }
}
