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

/** 카드를 뽑아 본 기록. 하루 뽑기 횟수를 세는 데만 쓴다 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prayer_card_draw")
public class PrayerCardDraw {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "card_id", nullable = false)
  private Long cardId;

  @Column(name = "drawer_uid", nullable = false)
  private Long drawerUid;

  @Column(name = "drawn_at", nullable = false)
  private LocalDateTime drawnAt;

  public PrayerCardDraw(Long cardId, Long drawerUid, LocalDateTime drawnAt) {
    this.cardId = cardId;
    this.drawerUid = drawerUid;
    this.drawnAt = drawnAt;
  }
}
