package com.gospelee.api.entity;

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

/** 기도 쪽지에 "기도했어요"를 남긴 기록 (카드당 한 사람 한 번) */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prayer_card_response", uniqueConstraints = @UniqueConstraint(
    name = "uk_prayer_card_response", columnNames = {"card_id", "responder_uid"}))
public class PrayerCardResponse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "card_id", nullable = false)
  private Long cardId;

  @Column(name = "responder_uid", nullable = false)
  private Long responderUid;

  @Column(name = "show_name", nullable = false)
  private boolean showName;

  @Column(name = "cheer_code", length = 20)
  private String cheerCode;

  @Column(name = "insert_time")
  private LocalDateTime insertTime;

  public PrayerCardResponse(Long cardId, Long responderUid, boolean showName, String cheerCode,
      LocalDateTime insertTime) {
    this.cardId = cardId;
    this.responderUid = responderUid;
    this.showName = showName;
    this.cheerCode = cheerCode;
    this.insertTime = insertTime;
  }
}
