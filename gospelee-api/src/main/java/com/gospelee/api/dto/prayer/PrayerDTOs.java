package com.gospelee.api.dto.prayer;

import com.gospelee.api.enums.PrayerEnums.AdminSeesAnon;
import com.gospelee.api.enums.PrayerEnums.AuthorMode;
import com.gospelee.api.enums.PrayerEnums.CardScope;
import com.gospelee.api.enums.PrayerEnums.CardStatus;
import com.gospelee.api.enums.PrayerEnums.DisplayMode;
import com.gospelee.api.enums.PrayerEnums.ModerationMode;
import com.gospelee.api.enums.PrayerEnums.ResponderVisibility;
import com.gospelee.api.enums.PrayerEnums.ScopeMode;
import com.gospelee.api.enums.PrayerEnums.WhoCanWrite;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 기도 쪽지 요청/응답 모음.
 * 응답에는 작성자 uid 를 담지 않는다 (익명성). 작성자 이름은 표시 방식이 NAME 인 카드에만 담는다.
 */
public final class PrayerDTOs {

  private PrayerDTOs() {
  }

  // ---------------------------------------------------------------------------
  // 요청
  // ---------------------------------------------------------------------------

  public record SettingRequest(
      @NotNull Boolean enabled,
      @NotNull AuthorMode authorMode,
      @NotNull Integer maxLength,
      @NotNull List<Integer> allowedExpiryDays,
      @NotNull WhoCanWrite whoCanWrite,
      @NotNull ScopeMode scopeMode,
      @NotNull ResponderVisibility responderVisibility,
      @NotNull AdminSeesAnon adminSeesAnon,
      @NotNull ModerationMode moderationMode,
      @NotNull Integer autoHideThreshold,
      @NotNull Boolean cheerEnabled,
      @NotNull Integer dailyDrawLimit,
      @NotNull Boolean summaryPush) {

  }

  public record TreeEventRequest(
      @NotNull Boolean enabled,
      LocalDateTime startAt,
      LocalDateTime endAt,
      Integer minCards) {

  }

  public record CardCreateRequest(
      @NotNull String content,
      DisplayMode displayMode,
      Integer expiryDays,
      CardScope scope,
      Long groupId) {

  }

  public record RespondRequest(Boolean showName, String cheerCode) {

  }

  public record ReportRequest(
      @NotNull @Size(max = 30) String reason,
      @Size(max = 300) String detail) {

  }

  // ---------------------------------------------------------------------------
  // 설정 응답
  // ---------------------------------------------------------------------------

  public record TreeEventValues(boolean enabled, LocalDateTime startAt, LocalDateTime endAt,
                                int minCards) {

  }

  /** 앱이 받는 설정. 성도에게 필요한 값만 담는다 */
  public record AppSettingResponse(
      boolean enabled,
      boolean canWrite,
      AuthorMode authorMode,
      int maxLength,
      List<Integer> allowedExpiryDays,
      ScopeMode scopeMode,
      ResponderVisibility responderVisibility,
      boolean cheerEnabled,
      TreeEventValues treeEvent) {

  }

  public record SettingValues(
      boolean enabled,
      AuthorMode authorMode,
      int maxLength,
      List<Integer> allowedExpiryDays,
      WhoCanWrite whoCanWrite,
      ScopeMode scopeMode,
      ResponderVisibility responderVisibility,
      AdminSeesAnon adminSeesAnon,
      ModerationMode moderationMode,
      int autoHideThreshold,
      boolean cheerEnabled,
      int dailyDrawLimit,
      boolean summaryPush) {

  }

  public record HistoryItem(String changedByName, LocalDateTime changedAt, String changes) {

  }

  public record AdminSettingResponse(SettingValues setting, TreeEventValues treeEvent,
                                     List<HistoryItem> history) {

  }

  // ---------------------------------------------------------------------------
  // 카드 응답
  // ---------------------------------------------------------------------------

  /** 다른 성도에게 보이는 카드. authorName 은 표시 방식이 NAME 일 때만 채운다 */
  public record CardView(
      Long id,
      String content,
      DisplayMode displayMode,
      String authorName,
      CardScope scope,
      LocalDateTime expiresAt,
      LocalDateTime createdAt,
      boolean responded,
      String responderNotice) {

  }

  public record DrawResponse(CardView card, Integer remainingDraws) {

  }

  public record RespondResponse(Long cardId, int responseCount, boolean alreadyResponded) {

  }

  public record MyCard(
      Long id,
      String content,
      DisplayMode displayMode,
      CardScope scope,
      CardStatus status,
      LocalDateTime expiresAt,
      LocalDateTime createdAt,
      int responseCount,
      List<String> responders,
      Map<String, Integer> cheers) {

  }

  /**
   * 교회 탭 배너용 요약. availableCards 는 지금 내가 뽑을 수 있는 쪽지 수, remainingDraws 는 오늘 남은 횟수
   * (무제한이면 null), newResponses 는 요청한 시각 이후 내 쪽지에 새로 달린 기도 수다.
   */
  public record SummaryResponse(long availableCards, Integer remainingDraws, long newResponses,
                                boolean treeRunning, Integer treeProgress) {

  }

  public record TreeCard(Long id, int slot, boolean lit) {

  }

  public record TreeResponse(List<TreeCard> cards, long litCount, long totalCount,
                             Integer progress, int minCards) {

  }

  // ---------------------------------------------------------------------------
  // 관리자 응답
  // ---------------------------------------------------------------------------

  public record AdminCardItem(
      Long id,
      String content,
      DisplayMode displayMode,
      CardStatus status,
      int reportCount,
      int responseCount,
      LocalDateTime createdAt,
      String authorName,
      List<String> reasons) {

  }

  public record AdminStats(
      long totalCards,
      long activeCards,
      long answeredCards,
      long hiddenCards,
      long pendingCards,
      long openWithoutResponse,
      long totalResponses,
      long pendingReportCards) {

  }
}
