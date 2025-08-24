package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.Yn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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

  @Column
  private String openYn;

  @Builder
  public Announcement(Long id, String organizationType, Long organizationId, String subject,
      String text, Long fileUid, String pushNotificationIds, String openYn) {
    this.id = id;
    this.organizationType = organizationType;
    this.organizationId = organizationId;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.pushNotificationIds = pushNotificationIds;
    this.openYn = openYn;
  }

  @PrePersist
  public void prePersist() {
    if (this.openYn == null) {
      this.openYn = Yn.N.name(); // Enum Yn의 N 값
    }
  }

  public void changePushNotificationIds(String pushNotificationIds) {
    this.pushNotificationIds = pushNotificationIds;
  }

  public void changeSubject(String subject) {
    this.subject = subject;
  }

  public void changeText(String text) {
    this.text = text;
  }

  public void changeOpenYn(Yn yn) {
    this.openYn = yn.name();
  }

}
