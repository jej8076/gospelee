package com.gospelee.api.dto.ministry;

import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import com.gospelee.api.enums.MinistryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** 모임 API 의 요청·응답 모음 */
public final class MinistryDtos {

  private MinistryDtos() {
  }

  public record SaveRequest(
      @NotBlank @Size(max = 30) String name,
      @Size(max = 200) String description) {

  }

  public record LeaveRequest(Long successorUid) {

  }

  public record RoleRequest(MinistryRole role) {

  }

  public record InviteSettingsRequest(boolean autoApprove) {

  }

  public record JoinRequest(String code) {

  }

  public record ReportRequest(String reason, String detail) {

  }

  public record DecisionRequest(boolean approve) {

  }

  /** 모임 한 건. inviteCode 는 대표·운영진에게만 채운다 */
  public record Detail(
      Long id, String name, String description, MinistryStatus status,
      MinistryRole myRole, MinistryMemberStatus myStatus, long memberCount,
      String inviteCode, Boolean inviteAutoApprove) {

  }

  public record Summary(
      Long id, String name, MinistryStatus status, MinistryRole role,
      MinistryMemberStatus memberStatus) {

  }

  public record Member(
      Long accountUid, String name, MinistryRole role, LocalDateTime joinedAt) {

  }

  public record Request(Long accountUid, String name, LocalDateTime requestedAt) {

  }

  public record Invite(String inviteCode, boolean autoApprove) {

  }

  public record InviteInfo(String name, String description, long memberCount) {

  }

  /** status: JOINED(즉시 가입) / PENDING(승인 대기) / ALREADY_JOINED */
  public record JoinResult(Long ministryId, String ministryName, String status) {

  }

  /** result: LEFT / TRANSFERRED(다른 사람이 대표가 됨) / DELETED(모임이 삭제됨) */
  public record LeaveResult(String result, Long newOwnerUid) {

  }
}
