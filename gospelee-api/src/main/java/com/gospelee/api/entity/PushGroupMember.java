package com.gospelee.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 푸시 알림 그룹에 속한 성도 (같은 계정은 한 그룹에 한 번만) */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "push_group_member", uniqueConstraints = @UniqueConstraint(
    name = "uk_push_group_member", columnNames = {"group_id", "account_uid"}))
public class PushGroupMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "group_id", nullable = false)
  private Long groupId;

  @Column(name = "account_uid", nullable = false)
  private Long accountUid;

  @Builder
  public PushGroupMember(Long id, Long groupId, Long accountUid) {
    this.id = id;
    this.groupId = groupId;
    this.accountUid = accountUid;
  }
}
