package com.gospelee.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 기도 쪽지 설정 변경 이력. 바뀐 항목만 JSON 으로 남긴다 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prayer_setting_history")
public class PrayerSettingHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "ecclesia_uid", nullable = false)
  private Long ecclesiaUid;

  @Column(name = "changed_by", nullable = false)
  private Long changedBy;

  @Column(name = "changes", nullable = false, columnDefinition = "TEXT")
  private String changes;

  @Column(name = "changed_at", nullable = false)
  private LocalDateTime changedAt;

  public PrayerSettingHistory(Long ecclesiaUid, Long changedBy, String changes,
      LocalDateTime changedAt) {
    this.ecclesiaUid = ecclesiaUid;
    this.changedBy = changedBy;
    this.changes = changes;
    this.changedAt = changedAt;
  }
}
