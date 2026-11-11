package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.MinistryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 모임. 교회({@link Ecclesia})와 달리 한 사람이 여러 모임에 속할 수 있어 별도 테이블로 둔다.
 * 대표는 컬럼이 아니라 {@link MinistryMember} 의 OWNER 역할로 표현한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "ministry")
public class Ministry extends EditInfomation {

  public static final long DEFAULT_STORAGE_LIMIT_BYTES = 100L * 1024 * 1024;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false, length = 30)
  private String name;

  @Column(name = "description", length = 200)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private MinistryStatus status;

  @Column(name = "invite_code", length = 32)
  private String inviteCode;

  @Column(name = "invite_auto_approve", nullable = false)
  private boolean inviteAutoApprove;

  @Column(name = "storage_limit_bytes", nullable = false)
  private long storageLimitBytes;

  @Column(name = "storage_used_bytes", nullable = false)
  private long storageUsedBytes;

  @Builder
  public Ministry(String name, String description, String inviteCode) {
    this.name = name;
    this.description = description;
    this.inviteCode = inviteCode;
    this.status = MinistryStatus.ACTIVE;
    this.storageLimitBytes = DEFAULT_STORAGE_LIMIT_BYTES;
  }

  public void change(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public void changeInviteCode(String inviteCode) {
    this.inviteCode = inviteCode;
  }

  public void changeInviteAutoApprove(boolean autoApprove) {
    this.inviteAutoApprove = autoApprove;
  }

  public void changeStatus(MinistryStatus status) {
    this.status = status;
  }

  public boolean isSuspended() {
    return status == MinistryStatus.SUSPENDED;
  }
}
