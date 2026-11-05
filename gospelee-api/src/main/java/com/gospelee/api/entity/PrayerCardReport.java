package com.gospelee.api.entity;

import com.gospelee.api.enums.PrayerEnums.ReportStatus;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 기도 쪽지 신고 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prayer_card_report", uniqueConstraints = @UniqueConstraint(
    name = "uk_prayer_card_report", columnNames = {"card_id", "reporter_uid"}))
public class PrayerCardReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "card_id", nullable = false)
  private Long cardId;

  @Column(name = "reporter_uid", nullable = false)
  private Long reporterUid;

  @Column(name = "reason", nullable = false, length = 30)
  private String reason;

  @Column(name = "detail", length = 300)
  private String detail;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private ReportStatus status;

  @Column(name = "insert_time")
  private LocalDateTime insertTime;

  public PrayerCardReport(Long cardId, Long reporterUid, String reason, String detail,
      LocalDateTime insertTime) {
    this.cardId = cardId;
    this.reporterUid = reporterUid;
    this.reason = reason;
    this.detail = detail;
    this.status = ReportStatus.PENDING;
    this.insertTime = insertTime;
  }

  public void close(ReportStatus status) {
    this.status = status;
  }
}
