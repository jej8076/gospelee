package com.gospelee.api.entity;

import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Announcement extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column
  private String organizationType;

  // EcclesiaUid or ..
  @Column
  private Long organizationId;

  @Column
  private String subject;

  @Column
  private String text;

  @Column
  private Long fileUid;

  @Column(name = "push_notification_ids")
  private String pushNotificationIds;

  @Builder
  public Announcement(Long id, String organizationType, Long organizationId, String subject,
      String text, Long fileUid, String pushNotificationIds) {
    this.id = id;
    this.organizationType = organizationType;
    this.organizationId = organizationId;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.pushNotificationIds = pushNotificationIds;
  }

  public void changePushNotificationIds(String pushNotificationIds) {
    this.pushNotificationIds = pushNotificationIds;
  }


}
