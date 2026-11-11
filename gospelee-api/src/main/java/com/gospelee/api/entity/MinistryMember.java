package com.gospelee.api.entity;

import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 모임 멤버십. (모임, 계정) 당 한 행이며 내보낸 이력도 REMOVED 상태로 남긴다 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "ministry_member", uniqueConstraints = @UniqueConstraint(
    name = "uk_ministry_member", columnNames = {"ministry_id", "account_uid"}))
public class MinistryMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "ministry_id", nullable = false)
  private Long ministryId;

  @Column(name = "account_uid", nullable = false)
  private Long accountUid;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 10)
  private MinistryRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private MinistryMemberStatus status;

  /** 승인되어 ACTIVE 가 된 시각. 대표 승계 순서의 기준 */
  @Column(name = "joined_at")
  private LocalDateTime joinedAt;

  @Column(name = "insert_time")
  private LocalDateTime insertTime;

  @Builder
  public MinistryMember(Long ministryId, Long accountUid, MinistryRole role,
      MinistryMemberStatus status, LocalDateTime joinedAt) {
    this.ministryId = ministryId;
    this.accountUid = accountUid;
    this.role = role;
    this.status = status;
    this.joinedAt = joinedAt;
    this.insertTime = LocalDateTime.now();
  }

  public boolean isActive() {
    return status == MinistryMemberStatus.ACTIVE;
  }

  public void changeRole(MinistryRole role) {
    this.role = role;
  }

  public void approve() {
    this.status = MinistryMemberStatus.ACTIVE;
    this.joinedAt = LocalDateTime.now();
  }

  public void requestAgain() {
    this.status = MinistryMemberStatus.PENDING;
    this.role = MinistryRole.MEMBER;
    this.joinedAt = null;
  }

  public void remove() {
    this.status = MinistryMemberStatus.REMOVED;
    this.role = MinistryRole.MEMBER;
    this.joinedAt = null;
  }
}
