package com.gospelee.api.dto.announcement;

import com.gospelee.api.entity.Announcement;
import jakarta.validation.constraints.NotBlank;
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
  @NotBlank
  private String subject;
  private String text;
  private Long fileUid;
  @NotBlank
  private String pushNotificationSendYn;

  @Builder
  public AnnouncementDTO(Long id, String organizationType, String subject, String text,
      Long fileUid,
      String pushNotificationSendYn) {
    this.id = id;
    this.organizationType = organizationType;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.pushNotificationSendYn = pushNotificationSendYn;
  }

  public static AnnouncementDTO fromEntity(Announcement announcement) {
    return AnnouncementDTO.builder()
        .id(announcement.getId())
        .organizationType(announcement.getOrganizationType())
        .subject(announcement.getSubject())
        .text(announcement.getText())
        .fileUid(announcement.getFileUid())
        .build();
  }

  public Announcement toEntity() {
    return Announcement.builder()
        .id(this.id)
        .organizationType(this.organizationType)
        .subject(this.subject)
        .text(this.text)
        .fileUid(this.fileUid)
        .build();
  }
}
