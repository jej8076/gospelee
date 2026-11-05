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

/** 교회별 기도 트리(크리스마스 등) 이벤트 설정 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prayer_tree_event", uniqueConstraints = @UniqueConstraint(
    name = "uk_prayer_tree_event_ecclesia", columnNames = {"ecclesia_uid"}))
public class PrayerTreeEvent extends EditInfomation {

  public static final int DEFAULT_MIN_CARDS = 10;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "ecclesia_uid", nullable = false)
  private Long ecclesiaUid;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Column(name = "start_at")
  private LocalDateTime startAt;

  @Column(name = "end_at")
  private LocalDateTime endAt;

  @Column(name = "min_cards", nullable = false)
  private int minCards;

  public PrayerTreeEvent(Long ecclesiaUid) {
    this.ecclesiaUid = ecclesiaUid;
    this.enabled = false;
    this.minCards = DEFAULT_MIN_CARDS;
  }

  public void apply(boolean enabled, LocalDateTime startAt, LocalDateTime endAt, int minCards) {
    this.enabled = enabled;
    this.startAt = startAt;
    this.endAt = endAt;
    this.minCards = minCards;
  }

  /** 켜져 있고 기간 안인지 (시작/종료가 비어 있으면 그쪽은 제한 없음) */
  public boolean isRunning(LocalDateTime now) {
    return enabled
        && (startAt == null || !now.isBefore(startAt))
        && (endAt == null || !now.isAfter(endAt));
  }
}
