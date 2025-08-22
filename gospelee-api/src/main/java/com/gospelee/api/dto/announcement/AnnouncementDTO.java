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
  private String openYn;
  private LocalDateTime insertTime;
  private LocalDateTime updateTime;

  // blob URL과 파일명 매핑을 위한 필드 (요청 시에만 사용)
  private Map<String, String> blobFileMapping;

  // 파일 데이터 리스트 (응답 시에만 사용) - Base64 인코딩된 파일 데이터
  private List<String> fileDataList;

  // 삭제할 파일 상세 ID 목록 (요청 시에만 사용)
  private List<Long> deleteFileDetailIdList;

  @Builder
  public AnnouncementDTO(Long id, String organizationType, String organizationId, String subject,
      String text, Long fileUid, List<FileDetailsDTO> fileDetailList, String pushNotificationSendYn,
      String pushNotificationIds, String openYn, LocalDateTime insertTime, LocalDateTime updateTime,
      Map<String, String> blobFileMapping, List<String> fileDataList,
      List<Long> deleteFileDetailIdList) {
    this.id = id;
    this.organizationType = organizationType;
    this.organizationId = organizationId;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.fileDetailList = fileDetailList;
    this.pushNotificationSendYn = pushNotificationSendYn;
    this.pushNotificationIds = pushNotificationIds;
    this.openYn = openYn;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
    this.blobFileMapping = blobFileMapping;
    this.fileDataList = fileDataList;
    this.deleteFileDetailIdList = deleteFileDetailIdList;
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
        .openYn(announcement.getOpenYn())
        .insertTime(announcement.getInsertTime())
        .updateTime(announcement.getUpdateTime())
        .build();
  }

  public static AnnouncementDTO fromEntity(Announcement announcement,
      List<FileDetailsDTO> fileDetails, List<String> fileDataList) {
    return AnnouncementDTO.builder()
        .id(announcement.getId())
        .organizationType(announcement.getOrganizationType())
        .organizationId(String.valueOf(announcement.getOrganizationId()))
        .subject(announcement.getSubject())
        .text(announcement.getText())
        .fileUid(announcement.getFileUid())
        .fileDetailList(fileDetails)
        .fileDataList(fileDataList)
        .pushNotificationIds(announcement.getPushNotificationIds())
        .openYn(announcement.getOpenYn())
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
        .openYn(this.openYn)
        .build();
  }

  public void changeText(String text) {
    this.text = text;
  }
}
