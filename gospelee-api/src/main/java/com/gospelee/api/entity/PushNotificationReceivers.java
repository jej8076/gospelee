
package com.gospelee.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "PUSH_NOTIFICATION_RECEIVERS_GENERATOR",
    sequenceName = "PUSH_NOTIFICATION_RECEIVERS_SEQ01",
    allocationSize = 1
)
@ToString
public class PushNotificationReceivers {

  @Id
  @GeneratedValue(
      strategy = GenerationType.AUTO,
      generator = "PUSH_NOTIFICATION_RECEIVERS_GENERATOR"
  )
  private Long id;

  @Column
  private Long pushNotificationId;

  @Column
  private Long receiveAccountUid;

  @Column
  private String status;

}
