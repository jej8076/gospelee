package com.gospelee.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountMeta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column(name = "account_uid")
  private Long accountUid;

  @Column(length = 100)
  private String identifier;

  @Column(length = 100)
  private String email;

  @Column(name = "insert_time")
  private LocalDateTime insertTime;

  @Column(name = "insert_user", length = 255)
  private String insertUser;

  @Column(name = "update_time")
  private LocalDateTime updateTime;

  @Column(name = "update_user", length = 255)
  private String updateUser;

  @Builder
  public AccountMeta(Long uid, Long accountUid, String identifier, String email,
      LocalDateTime insertTime, String insertUser, LocalDateTime updateTime, String updateUser) {
    this.uid = uid;
    this.accountUid = accountUid;
    this.identifier = identifier;
    this.email = email;
    this.insertTime = insertTime;
    this.insertUser = insertUser;
    this.updateTime = updateTime;
    this.updateUser = updateUser;
  }
}
