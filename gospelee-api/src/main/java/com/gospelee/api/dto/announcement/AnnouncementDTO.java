package com.gospelee.api.dto.announcement;

import com.gospelee.api.entity.Announcement;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnouncementDTO {

  private Long id;
  private String subject;
  private String text;
  private Long fileUid;
  private String pushNotificationIds;

  @Builder
  public AnnouncementDTO(Long id, String subject, String text, Long fileUid,
      String pushNotificationIds) {
    this.id = id;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.pushNotificationIds = pushNotificationIds;
  }

  public Announcement toEntity() {
    return Announcement.builder()
        .id(this.id)
        .subject(this.subject)
        .text(this.text)
        .fileUid(this.fileUid)
        .pushNotificationIds(this.pushNotificationIds)
        .build();
  }

  public static AnnouncementDTO fromEntity(Announcement announcement) {
    return AnnouncementDTO.builder()
        .id(announcement.getId())
        .subject(announcement.getSubject())
        .text(announcement.getText())
        .fileUid(announcement.getFileUid())
        .pushNotificationIds(announcement.getPushNotificationIds())
        .build();
  }
}
