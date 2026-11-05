package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.PrayerEnums.AdminSeesAnon;
import com.gospelee.api.enums.PrayerEnums.AuthorMode;
import com.gospelee.api.enums.PrayerEnums.ModerationMode;
import com.gospelee.api.enums.PrayerEnums.ResponderVisibility;
import com.gospelee.api.enums.PrayerEnums.ScopeMode;
import com.gospelee.api.enums.PrayerEnums.WhoCanWrite;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 교회별 기도 쪽지 설정. 행이 없으면 {@link #defaults(Long)} 의 가장 보수적인 값으로 본다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "prayer_setting", uniqueConstraints = @UniqueConstraint(
    name = "uk_prayer_setting_ecclesia", columnNames = {"ecclesia_uid"}))
public class PrayerSetting extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "ecclesia_uid", nullable = false)
  private Long ecclesiaUid;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Enumerated(EnumType.STRING)
  @Column(name = "author_mode", nullable = false, length = 20)
  private AuthorMode authorMode;

  @Column(name = "max_length", nullable = false)
  private int maxLength;

  /** 쉼표로 구분한 노출 기간(일). 0 은 상시 */
  @Column(name = "allowed_expiry_days", nullable = false, length = 30)
  private String allowedExpiryDays;

  @Enumerated(EnumType.STRING)
  @Column(name = "who_can_write", nullable = false, length = 20)
  private WhoCanWrite whoCanWrite;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope_mode", nullable = false, length = 20)
  private ScopeMode scopeMode;

  @Enumerated(EnumType.STRING)
  @Column(name = "responder_visibility", nullable = false, length = 20)
  private ResponderVisibility responderVisibility;

  @Enumerated(EnumType.STRING)
  @Column(name = "admin_sees_anon", nullable = false, length = 20)
  private AdminSeesAnon adminSeesAnon;

  @Enumerated(EnumType.STRING)
  @Column(name = "moderation_mode", nullable = false, length = 20)
  private ModerationMode moderationMode;

  @Column(name = "auto_hide_threshold", nullable = false)
  private int autoHideThreshold;

  @Column(name = "cheer_enabled", nullable = false)
  private boolean cheerEnabled;

  /** 하루 뽑기 횟수. 0 은 무제한 */
  @Column(name = "daily_draw_limit", nullable = false)
  private int dailyDrawLimit;

  @Column(name = "summary_push", nullable = false)
  private boolean summaryPush;

  /** 설정이 없는 교회에 적용되는 가장 보수적인 기본값 (저장하지 않는다) */
  public static PrayerSetting defaults(Long ecclesiaUid) {
    PrayerSetting s = new PrayerSetting();
    s.ecclesiaUid = ecclesiaUid;
    s.enabled = false;
    s.authorMode = AuthorMode.AUTHOR_CHOICE;
    s.maxLength = 200;
    s.allowedExpiryDays = "7,30";
    s.whoCanWrite = WhoCanWrite.APPROVED_ONLY;
    s.scopeMode = ScopeMode.CHURCH_ONLY;
    s.responderVisibility = ResponderVisibility.HIDDEN;
    s.adminSeesAnon = AdminSeesAnon.NEVER;
    s.moderationMode = ModerationMode.POST_REPORT;
    s.autoHideThreshold = 3;
    s.cheerEnabled = true;
    s.dailyDrawLimit = 5;
    s.summaryPush = true;
    return s;
  }

  public boolean isPersisted() {
    return id != null;
  }

  /** 허용된 노출 기간(일) 목록 */
  public List<Integer> expiryDays() {
    return Arrays.stream(allowedExpiryDays.split(","))
        .map(String::trim).filter(v -> !v.isEmpty()).map(Integer::parseInt).toList();
  }

  public void apply(boolean enabled, AuthorMode authorMode, int maxLength,
      String allowedExpiryDays, WhoCanWrite whoCanWrite, ScopeMode scopeMode,
      ResponderVisibility responderVisibility, AdminSeesAnon adminSeesAnon,
      ModerationMode moderationMode, int autoHideThreshold, boolean cheerEnabled,
      int dailyDrawLimit, boolean summaryPush) {
    this.enabled = enabled;
    this.authorMode = authorMode;
    this.maxLength = maxLength;
    this.allowedExpiryDays = allowedExpiryDays;
    this.whoCanWrite = whoCanWrite;
    this.scopeMode = scopeMode;
    this.responderVisibility = responderVisibility;
    this.adminSeesAnon = adminSeesAnon;
    this.moderationMode = moderationMode;
    this.autoHideThreshold = autoHideThreshold;
    this.cheerEnabled = cheerEnabled;
    this.dailyDrawLimit = dailyDrawLimit;
    this.summaryPush = summaryPush;
  }
}
