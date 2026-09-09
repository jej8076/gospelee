package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "account_bible_reading_goal_member",
    indexes = {
        @Index(name = "idx_abrgm_goal_account", columnList = "goal_id, account_id", unique = true),
        @Index(name = "idx_abrgm_account_status", columnList = "account_id, status"),
        @Index(name = "idx_abrgm_goal_status", columnList = "goal_id, status")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleReadingGoalMember extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long idx;

  @Column(name = "goal_id", nullable = false)
  private Long goalIdx;

  @Column(name = "account_id", nullable = false)
  private Long accountUid;

  // HOST, MEMBER
  @Column(nullable = false, length = 20)
  private String role;

  // JOINED, LEFT
  @Column(nullable = false, length = 20)
  private String status;

  @Column(name = "joined_at", nullable = false)
  private LocalDateTime joinedAt;

  @Builder
  public AccountBibleReadingGoalMember(Long idx, Long goalIdx, Long accountUid, String role,
      String status, LocalDateTime joinedAt) {
    this.idx = idx;
    this.goalIdx = goalIdx;
    this.accountUid = accountUid;
    this.role = role != null ? role : "MEMBER";
    this.status = status != null ? status : "JOINED";
    this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
  }

  public void changeRole(String role) {
    this.role = role;
  }

  public void changeStatus(String status) {
    this.status = status;
  }

  public void rejoin() {
    this.status = "JOINED";
    this.joinedAt = LocalDateTime.now();
  }
}
