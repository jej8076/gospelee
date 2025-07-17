package com.gospelee.api.entity;

import com.gospelee.api.enums.AccountEcclesiaHistoryStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_ecclesia_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEcclesiaHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "accountUid", nullable = false)
  private long accountUid;

  @Column(name = "ecclesiaUid", nullable = false)
  private long ecclesiaUid;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AccountEcclesiaHistoryStatusType status;

  @Column(name = "insert_time", nullable = false, updatable = false)
  private LocalDateTime insertTime;

  @Builder
  public AccountEcclesiaHistory(Long id, long accountUid, long ecclesiaUid,
      AccountEcclesiaHistoryStatusType status, LocalDateTime insertTime) {
    this.id = id;
    this.accountUid = accountUid;
    this.ecclesiaUid = ecclesiaUid;
    this.status = status;
    this.insertTime = insertTime;
  }

  @PrePersist
  protected void onCreate() {
    this.insertTime = LocalDateTime.now();
  }
}
