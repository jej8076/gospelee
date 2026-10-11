package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 공유받은 묵상: 로그인한 사용자가 남의 공유 링크를 열어 본 기록 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "journal_share_received", uniqueConstraints = @UniqueConstraint(
    name = "uk_journal_share_received", columnNames = {"accountUid", "shareUid"}))
public class JournalShareReceived extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column(nullable = false)
  private Long accountUid;

  @Column(nullable = false)
  private Long shareUid;

  @Column(nullable = false)
  private LocalDateTime lastViewedAt;

  public JournalShareReceived(Long accountUid, Long shareUid) {
    this.accountUid = accountUid;
    this.shareUid = shareUid;
    this.lastViewedAt = LocalDateTime.now();
  }

  public void touch() {
    this.lastViewedAt = LocalDateTime.now();
  }
}
