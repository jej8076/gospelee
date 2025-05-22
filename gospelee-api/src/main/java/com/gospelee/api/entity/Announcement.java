package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "ANNOUNCEMENT_GENERATOR",
    sequenceName = "ANNOUNCEMENT_SEQ01",
    allocationSize = 1
)
@ToString
public class Announcement extends EditInfomation {

  @Id
  @GeneratedValue(
      strategy = GenerationType.AUTO,
      generator = "ANNOUNCEMENT_GENERATOR"
  )
  private Long id;

  @Column
  private String subject;

  @Column
  private String text;

  @Column
  private Long fileUid;

  @Column(name = "push_notification_ids")
  private String pushNotificationIds;

  @Builder
  public Announcement(Long id, String subject, String text, Long fileUid,
      String pushNotificationIds) {
    this.id = id;
    this.subject = subject;
    this.text = text;
    this.fileUid = fileUid;
    this.pushNotificationIds = pushNotificationIds;
  }
}
