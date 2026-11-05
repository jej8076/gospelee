package com.gospelee.api.service.prayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.prayer.PrayerDTOs.AdminSettingResponse;
import com.gospelee.api.dto.prayer.PrayerDTOs.AppSettingResponse;
import com.gospelee.api.dto.prayer.PrayerDTOs.HistoryItem;
import com.gospelee.api.dto.prayer.PrayerDTOs.SettingRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.SettingValues;
import com.gospelee.api.dto.prayer.PrayerDTOs.TreeEventRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.TreeEventValues;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.entity.PrayerSettingHistory;
import com.gospelee.api.entity.PrayerTreeEvent;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerSettingHistoryRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerSettingRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerTreeEventRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 교회별 기도 쪽지 설정. 교회 관리자만 바꿀 수 있고, 바뀐 항목은 변경 이력으로 남긴다.
 * 설정이 없는 교회에는 가장 보수적인 기본값(꺼짐)이 적용된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrayerSettingService {

  static final int MIN_LENGTH = 50;
  static final int MAX_LENGTH = 300;
  static final int MAX_AUTO_HIDE_THRESHOLD = 10;
  static final int MAX_DAILY_DRAW_LIMIT = 50;
  static final int MAX_EXPIRY_DAYS = 365;
  static final int MAX_MIN_CARDS = 1000;

  private final PrayerSettingRepository settingRepository;
  private final PrayerSettingHistoryRepository historyRepository;
  private final PrayerTreeEventRepository treeEventRepository;
  private final AccountRepository accountRepository;
  private final ObjectMapper objectMapper;

  /** 저장된 설정, 없으면 기본값 (저장하지 않는다) */
  @Transactional(readOnly = true)
  public PrayerSetting getEffective(Long ecclesiaUid) {
    return settingRepository.findByEcclesiaUid(ecclesiaUid)
        .orElseGet(() -> PrayerSetting.defaults(ecclesiaUid));
  }

  @Transactional(readOnly = true)
  public PrayerTreeEvent getTreeEvent(Long ecclesiaUid) {
    return treeEventRepository.findByEcclesiaUid(ecclesiaUid)
        .orElseGet(() -> new PrayerTreeEvent(ecclesiaUid));
  }

  /** 앱용 설정. 교회가 없거나 꺼져 있으면 enabled=false 로 내려가 앱이 메뉴를 숨긴다 */
  @Transactional(readOnly = true)
  public AppSettingResponse appSetting(AccountAuthDTO account) {
    if (!PrayerAccess.isMember(account)) {
      return toApp(PrayerSetting.defaults(null), new PrayerTreeEvent(null), false);
    }
    PrayerSetting setting = getEffective(account.getEcclesiaUid());
    PrayerTreeEvent tree = getTreeEvent(account.getEcclesiaUid());
    boolean canWrite = setting.isEnabled() && PrayerAccess.canParticipate(account, setting);
    return toApp(setting, tree, canWrite);
  }

  @Transactional(readOnly = true)
  public AdminSettingResponse adminSetting(AccountAuthDTO account) {
    PrayerAccess.requireManager(account);
    Long ecclesiaUid = account.getEcclesiaUid();
    List<PrayerSettingHistory> histories =
        historyRepository.findTop20ByEcclesiaUidOrderByChangedAtDesc(ecclesiaUid);
    Map<Long, String> names = accountRepository.findAllById(
            histories.stream().map(PrayerSettingHistory::getChangedBy).distinct().toList())
        .stream().collect(Collectors.toMap(Account::getUid,
            a -> a.getName() == null ? "" : a.getName(), (a, b) -> a));
    return new AdminSettingResponse(
        toValues(getEffective(ecclesiaUid)),
        toTreeValues(getTreeEvent(ecclesiaUid)),
        histories.stream()
            .map(h -> new HistoryItem(names.getOrDefault(h.getChangedBy(), ""),
                h.getChangedAt(), h.getChanges()))
            .toList());
  }

  @Transactional
  public AdminSettingResponse updateSetting(AccountAuthDTO account, SettingRequest request) {
    PrayerAccess.requireManager(account);
    Long ecclesiaUid = account.getEcclesiaUid();

    int maxLength = request.maxLength();
    if (maxLength < MIN_LENGTH || maxLength > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "글자 수 제한은 " + MIN_LENGTH + "~" + MAX_LENGTH + "자 사이여야 합니다.");
    }
    if (request.autoHideThreshold() < 1
        || request.autoHideThreshold() > MAX_AUTO_HIDE_THRESHOLD) {
      throw new IllegalArgumentException(
          "자동 숨김 신고 수는 1~" + MAX_AUTO_HIDE_THRESHOLD + " 사이여야 합니다.");
    }
    if (request.dailyDrawLimit() < 0 || request.dailyDrawLimit() > MAX_DAILY_DRAW_LIMIT) {
      throw new IllegalArgumentException(
          "하루 뽑기 횟수는 0(무제한)~" + MAX_DAILY_DRAW_LIMIT + " 사이여야 합니다.");
    }
    String expiryDays = normalizeExpiryDays(request.allowedExpiryDays());

    PrayerSetting setting = settingRepository.findByEcclesiaUid(ecclesiaUid)
        .orElseGet(() -> PrayerSetting.defaults(ecclesiaUid));
    Map<String, Object> before = snapshot(setting);
    setting.apply(request.enabled(), request.authorMode(), maxLength, expiryDays,
        request.whoCanWrite(), request.scopeMode(), request.responderVisibility(),
        request.adminSeesAnon(), request.moderationMode(), request.autoHideThreshold(),
        request.cheerEnabled(), request.dailyDrawLimit(), request.summaryPush());
    Map<String, Object> changes = diff(before, snapshot(setting));

    // 새 교회는 처음 저장할 때 행을 만든다. 기본값과 같아도 저장해 두어야 이후 이력이 이어진다
    if (!setting.isPersisted() || !changes.isEmpty()) {
      settingRepository.save(setting);
    }
    recordHistory(ecclesiaUid, account.getUid(), changes);
    return adminSetting(account);
  }

  @Transactional
  public AdminSettingResponse updateTreeEvent(AccountAuthDTO account, TreeEventRequest request) {
    PrayerAccess.requireManager(account);
    Long ecclesiaUid = account.getEcclesiaUid();

    int minCards = request.minCards() == null ? PrayerTreeEvent.DEFAULT_MIN_CARDS
        : request.minCards();
    if (minCards < 1 || minCards > MAX_MIN_CARDS) {
      throw new IllegalArgumentException("최소 카드 수는 1~" + MAX_MIN_CARDS + " 사이여야 합니다.");
    }
    if (request.startAt() != null && request.endAt() != null
        && request.endAt().isBefore(request.startAt())) {
      throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
    }

    PrayerTreeEvent event = treeEventRepository.findByEcclesiaUid(ecclesiaUid)
        .orElseGet(() -> new PrayerTreeEvent(ecclesiaUid));
    Map<String, Object> before = treeSnapshot(event);
    event.apply(request.enabled(), request.startAt(), request.endAt(), minCards);
    Map<String, Object> changes = diff(before, treeSnapshot(event));

    if (event.getId() == null || !changes.isEmpty()) {
      treeEventRepository.save(event);
    }
    recordHistory(ecclesiaUid, account.getUid(), changes);
    return adminSetting(account);
  }

  // ---------------------------------------------------------------------------

  /** 쉼표로 이어 붙인 노출 기간. 중복을 빼고 작은 값부터 정렬한다 (0 = 상시) */
  static String normalizeExpiryDays(List<Integer> days) {
    if (days == null || days.isEmpty()) {
      throw new IllegalArgumentException("노출 기간을 하나 이상 선택해 주세요.");
    }
    for (Integer d : days) {
      if (d == null || d < 0 || d > MAX_EXPIRY_DAYS) {
        throw new IllegalArgumentException(
            "노출 기간은 0(상시)~" + MAX_EXPIRY_DAYS + "일 사이여야 합니다.");
      }
    }
    return days.stream().distinct().sorted().map(String::valueOf)
        .collect(Collectors.joining(","));
  }

  private void recordHistory(Long ecclesiaUid, Long changedBy, Map<String, Object> changes) {
    if (changes.isEmpty()) {
      return;
    }
    try {
      historyRepository.save(new PrayerSettingHistory(ecclesiaUid, changedBy,
          objectMapper.writeValueAsString(changes), LocalDateTime.now()));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("설정 변경 이력을 만들지 못했습니다.", e);
    }
    log.info("[PRAYER] 설정 변경 ecclesiaUid:{}, accountUid:{}, 항목:{}", ecclesiaUid, changedBy,
        changes.keySet());
  }

  /** 바뀐 항목만 {"항목": {"from": 이전, "to": 이후}} 로 만든다 */
  static Map<String, Object> diff(Map<String, Object> before, Map<String, Object> after) {
    Map<String, Object> changes = new LinkedHashMap<>();
    after.forEach((key, value) -> {
      Object old = before.get(key);
      if (!Objects.equals(old, value)) {
        Map<String, Object> change = new LinkedHashMap<>();
        change.put("from", old == null ? null : old.toString());
        change.put("to", value == null ? null : value.toString());
        changes.put(key, change);
      }
    });
    return changes;
  }

  private static Map<String, Object> snapshot(PrayerSetting s) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("enabled", s.isEnabled());
    m.put("authorMode", s.getAuthorMode());
    m.put("maxLength", s.getMaxLength());
    m.put("allowedExpiryDays", s.getAllowedExpiryDays());
    m.put("whoCanWrite", s.getWhoCanWrite());
    m.put("scopeMode", s.getScopeMode());
    m.put("responderVisibility", s.getResponderVisibility());
    m.put("adminSeesAnon", s.getAdminSeesAnon());
    m.put("moderationMode", s.getModerationMode());
    m.put("autoHideThreshold", s.getAutoHideThreshold());
    m.put("cheerEnabled", s.isCheerEnabled());
    m.put("dailyDrawLimit", s.getDailyDrawLimit());
    m.put("summaryPush", s.isSummaryPush());
    return m;
  }

  private static Map<String, Object> treeSnapshot(PrayerTreeEvent e) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("tree.enabled", e.isEnabled());
    m.put("tree.startAt", e.getStartAt());
    m.put("tree.endAt", e.getEndAt());
    m.put("tree.minCards", e.getMinCards());
    return m;
  }

  private static SettingValues toValues(PrayerSetting s) {
    return new SettingValues(s.isEnabled(), s.getAuthorMode(), s.getMaxLength(), s.expiryDays(),
        s.getWhoCanWrite(), s.getScopeMode(), s.getResponderVisibility(), s.getAdminSeesAnon(),
        s.getModerationMode(), s.getAutoHideThreshold(), s.isCheerEnabled(),
        s.getDailyDrawLimit(), s.isSummaryPush());
  }

  private static TreeEventValues toTreeValues(PrayerTreeEvent e) {
    return new TreeEventValues(e.isEnabled(), e.getStartAt(), e.getEndAt(), e.getMinCards());
  }

  private static AppSettingResponse toApp(PrayerSetting s, PrayerTreeEvent tree,
      boolean canWrite) {
    return new AppSettingResponse(s.isEnabled(), canWrite, s.getAuthorMode(), s.getMaxLength(),
        s.expiryDays(), s.getScopeMode(), s.getResponderVisibility(), s.isCheerEnabled(),
        toTreeValues(tree));
  }
}
