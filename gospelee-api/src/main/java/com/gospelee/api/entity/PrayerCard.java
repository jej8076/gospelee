package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.PrayerEnums.AdminSeesAnon;
import com.gospelee.api.enums.PrayerEnums.CardScope;
import com.gospelee.api.enums.PrayerEnums.CardStatus;
import com.gospelee.api.enums.PrayerEnums.DisplayMode;
import com.gospelee.api.enums.PrayerEnums.ResponderVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기도 쪽지(기도제목 카드). 작성자 uid 는 신고 처리를 위해 저장하지만 API 응답으로는 내보내지 않는다.
 * responderVisible/adminSeesAnon 은 카드를 올린 시점의 교회 설정을 복사해 둔 값으로, 이후 설정이 바뀌어도 이 카드에는 적용하지 않는다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prayer_card")
public class PrayerCard extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "ecclesia_uid", nullable = false)
  private Long ecclesiaUid;

  @Column(name = "author_uid", nullable = false)
  private Long authorUid;

  @Column(name = "content", nullable = false, length = 300)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "display_mode", nullable = false, length = 10)
  private DisplayMode displayMode;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope", nullable = false, length = 10)
  private CardScope scope;

  @Column(name = "group_id")
  private Long groupId;

  /** null 이면 상시 */
  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private CardStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "responder_visible", nullable = false, length = 20)
  private ResponderVisibility responderVisible;

  @Enumerated(EnumType.STRING)
  @Column(name = "admin_sees_anon", nullable = false, length = 20)
  private AdminSeesAnon adminSeesAnon;

  @Column(name = "response_count", nullable = false)
  private int responseCount;

  @Column(name = "report_count", nullable = false)
  private int reportCount;

  @Builder
  public PrayerCard(Long id, Long ecclesiaUid, Long authorUid, String content,
      DisplayMode displayMode, CardScope scope, Long groupId, LocalDateTime expiresAt,
      CardStatus status, ResponderVisibility responderVisible, AdminSeesAnon adminSeesAnon) {
    this.id = id;
    this.ecclesiaUid = ecclesiaUid;
    this.authorUid = authorUid;
    this.content = content;
    this.displayMode = displayMode;
    this.scope = scope;
    this.groupId = groupId;
    this.expiresAt = expiresAt;
    this.status = status;
    this.responderVisible = responderVisible;
    this.adminSeesAnon = adminSeesAnon;
  }

  public boolean isExpired(LocalDateTime now) {
    return expiresAt != null && !expiresAt.isAfter(now);
  }

  /** 지금 다른 성도에게 보일 수 있는 상태 */
  public boolean isOpen(LocalDateTime now) {
    return status == CardStatus.ACTIVE && !isExpired(now);
  }

  public void changeStatus(CardStatus status) {
    this.status = status;
  }

  public void increaseReportCount() {
    this.reportCount++;
  }
}
