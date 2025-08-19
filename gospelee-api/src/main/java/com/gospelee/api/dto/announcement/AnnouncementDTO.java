package com.gospelee.api.dto.announcement;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.file.FileDetailsDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
  private String subject;
  private String text;
  private Long fileUid;
  private List<FileDetailsDTO> fileDetailList;
  @NotBlank
  private String pushNotificationSendYn;
  private String pushNotificationIds;
  private LocalDateTime insertTime;
  private LocalDateTime updateTime;

  // blob URL과 파일명 매핑을 위한 필드 (요청 시에만 사용)
  private Map<String, String> blobFileMapping;

  @Builder
  public AnnouncementDTO(Long id, String organizationType, String organizationId, String subject,
      String text, Long fileUid, List<FileDetailsDTO> fileDetailList, String pushNotificationSendYn,
      String pushNotificationIds, LocalDateTime insertTime, LocalDateTime updateTime,
      Map<String, String> blobFileMapping) {
    this.id = id;
    this.organizationType = organizationType;
    this.organizationId = organizationId;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.fileDetailList = fileDetailList;
    this.pushNotificationSendYn = pushNotificationSendYn;
    this.pushNotificationIds = pushNotificationIds;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
    this.blobFileMapping = blobFileMapping;
  }

  public static AnnouncementDTO fromEntity(Announcement announcement,
      List<FileDetailsDTO> fileDetails) {
    return AnnouncementDTO.builder()
        .id(announcement.getId())
        .organizationType(announcement.getOrganizationType())
        .organizationId(String.valueOf(announcement.getOrganizationId()))
        .subject(announcement.getSubject())
        .text(announcement.getText())
        .fileUid(announcement.getFileUid())
        .fileDetailList(fileDetails)
        .pushNotificationIds(announcement.getPushNotificationIds())
        .insertTime(announcement.getInsertTime())
        .updateTime(announcement.getUpdateTime())
        .build();
  }

  public static AnnouncementDTO fromEntity(Announcement announcement) {
    return AnnouncementDTO.fromEntity(announcement, null);
  }

  public Announcement toEntity(AccountAuthDTO accountAuthDTO) {
    return Announcement.builder()
        .id(this.id)
        .organizationType(OrganizationType.fromName(this.organizationType).name())
        .organizationId(accountAuthDTO.getEcclesiaUid())
        .subject(this.subject)
        .text(this.text)
        .fileUid(this.fileUid)
        .build();
  }

  public void changeText(String text) {
    this.text = text;
  }
}
