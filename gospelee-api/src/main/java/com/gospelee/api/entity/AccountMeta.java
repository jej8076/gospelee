package com.gospelee.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.LocalDateTime;
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
}
