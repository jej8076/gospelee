package com.gospelee.api.dto.announcement;

import com.gospelee.api.dto.file.FileDetailsDTO;
import com.gospelee.api.entity.Announcement;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
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
  private String imageAccessToken;
  private List<FileDetailsDTO> fileDetailList;
  @NotBlank
  private String pushNotificationSendYn;
  private String pushNotificationIds;
  private LocalDateTime insertTime;
  private LocalDateTime updateTime;

  @Builder
  @QueryProjection
  public AnnouncementResponseDTO(Long id, String organizationType, Long organizationId,
      String organizationName, String subject, String text, Long fileUid,
      List<FileDetailsDTO> fileDetailList, String pushNotificationSendYn,
      String pushNotificationIds,
      LocalDateTime insertTime, LocalDateTime updateTime) {
    this.id = id;
    this.organizationType = organizationType;
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.fileDetailList = fileDetailList;
    this.pushNotificationSendYn = pushNotificationSendYn;
    this.pushNotificationIds = pushNotificationIds;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
  }

  // QueryDSL용 생성자 (fileDetailList 제외)
  @QueryProjection
  public AnnouncementResponseDTO(Long id, String organizationType, Long organizationId,
      String organizationName, String subject, String text, Long fileUid,
      String pushNotificationSendYn, String pushNotificationIds,
      LocalDateTime insertTime, LocalDateTime updateTime) {
    this.id = id;
    this.organizationType = organizationType;
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.fileDetailList = null; // 나중에 별도로 설정
    this.pushNotificationSendYn = pushNotificationSendYn;
    this.pushNotificationIds = pushNotificationIds;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
  }

  public static AnnouncementResponseDTO fromEntity(Announcement entity) {
    return AnnouncementResponseDTO.builder()
        .id(entity.getId())
        .organizationType(entity.getOrganizationType())
        .organizationId(entity.getOrganizationId())
        .subject(entity.getSubject())
        .text(entity.getText())
        .fileUid(entity.getFileUid())
        .pushNotificationSendYn(
            (entity.getPushNotificationIds() != null && !entity.getPushNotificationIds().isEmpty())
                ? "Y" : "N")
        .pushNotificationIds(entity.getPushNotificationIds())
        .insertTime(entity.getInsertTime())
        .updateTime(entity.getUpdateTime())
        .build();
  }

  public static Announcement toEntity(AnnouncementResponseDTO dto) {
    return Announcement.builder()
        .id(dto.getId())
        .organizationType(dto.getOrganizationType())
        .organizationId(dto.getOrganizationId())
        .subject(dto.getSubject())
        .text(dto.getText())
        .fileUid(dto.getFileUid())
        .pushNotificationIds(dto.getPushNotificationIds())
        .build();
  }

  public void changeImageAccessToken(String accessToken) {
    this.imageAccessToken = accessToken;
  }

  public void changeFileDetail(List<FileDetailsDTO> fileList) {
    this.fileDetailList = fileList;
  }
}
