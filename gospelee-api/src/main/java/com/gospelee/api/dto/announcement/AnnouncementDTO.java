package com.gospelee.api.dto.announcement;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnouncementDTO {

  private Long id;
  @NotBlank
  private String organizationType;
  private String organizationId;
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
  public AnnouncementDTO(Long id, String organizationType, String organizationId, String subject,
      String text, Long fileUid, String pushNotificationSendYn, String pushNotificationIds,
      LocalDateTime insertTime, LocalDateTime updateTime) {
    this.id = id;
    this.organizationType = organizationType;
    this.organizationId = organizationId;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.pushNotificationSendYn = pushNotificationSendYn;
    this.pushNotificationIds = pushNotificationIds;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
  }

  public static AnnouncementDTO fromEntity(Announcement announcement) {
    return AnnouncementDTO.builder()
        .id(announcement.getId())
        .organizationType(announcement.getOrganizationType())
        .organizationId(String.valueOf(announcement.getOrganizationId()))
        .subject(announcement.getSubject())
        .text(announcement.getText())
        .fileUid(announcement.getFileUid())
        .pushNotificationIds(announcement.getPushNotificationIds())
        .insertTime(announcement.getInsertTime())
        .updateTime(announcement.getUpdateTime())
        .build();
  }

  public Announcement toEntity(AccountAuthDTO accountAuthDTO) {
    return Announcement.builder()
        .id(this.id)
        .organizationType(OrganizationType.fromName(this.organizationType).name())
        .organizationId(Long.valueOf(accountAuthDTO.getEcclesiaUid()))
        .subject(this.subject)
        .text(this.text)
        .fileUid(this.fileUid)
        .build();
  }
}
