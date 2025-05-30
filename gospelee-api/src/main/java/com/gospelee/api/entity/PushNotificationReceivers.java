package com.gospelee.api.entity;

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
@ToString
public class PushNotificationReceivers {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column
  private Long pushNotificationId;

  @Column
  private Long receiveAccountUid;

  @Column
  private String status;

  @Builder
  public PushNotificationReceivers(Long id, Long pushNotificationId, Long receiveAccountUid,
      String status) {
    this.id = id;
    this.pushNotificationId = pushNotificationId;
    this.receiveAccountUid = receiveAccountUid;
    this.status = status;
  }
}
