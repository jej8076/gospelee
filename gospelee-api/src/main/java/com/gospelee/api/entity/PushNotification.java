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
@ToString
public class PushNotification extends EditInfomation {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column
  private Long sendAccountUid;

  @Column
  private String title;

  @Column
  private String message;

  @Column
  private String organization;

  @Column
  private String category;

  @Column
  private Integer totalCount;

  @Builder
  public PushNotification(Long id, Long sendAccountUid, String title, String message,
      String organization, String category, Integer totalCount) {
    this.id = id;
    this.sendAccountUid = sendAccountUid;
    this.title = title;
    this.message = message;
    this.organization = organization;
    this.category = category;
    this.totalCount = totalCount;
  }
}
