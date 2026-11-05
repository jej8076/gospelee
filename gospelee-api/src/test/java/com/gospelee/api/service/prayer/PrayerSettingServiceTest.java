package com.gospelee.api.service.prayer;

import static com.gospelee.api.service.prayer.PrayerTestFixtures.ECCLESIA;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.manager;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.member;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.dto.prayer.PrayerDTOs.AppSettingResponse;
import com.gospelee.api.dto.prayer.PrayerDTOs.SettingRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.TreeEventRequest;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.entity.PrayerSettingHistory;
import com.gospelee.api.enums.PrayerEnums.AdminSeesAnon;
import com.gospelee.api.enums.PrayerEnums.AuthorMode;
import com.gospelee.api.enums.PrayerEnums.ModerationMode;
import com.gospelee.api.enums.PrayerEnums.ResponderVisibility;
import com.gospelee.api.enums.PrayerEnums.ScopeMode;
import com.gospelee.api.enums.PrayerEnums.WhoCanWrite;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerSettingHistoryRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerSettingRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerTreeEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

class PrayerSettingServiceTest {

  private PrayerSettingRepository settingRepository;
  private PrayerSettingHistoryRepository historyRepository;
  private PrayerTreeEventRepository treeEventRepository;
  private PrayerSettingService service;

  @BeforeEach
  void setUp() {
    settingRepository = mock(PrayerSettingRepository.class);
    historyRepository = mock(PrayerSettingHistoryRepository.class);
    treeEventRepository = mock(PrayerTreeEventRepository.class);
    service = new PrayerSettingService(settingRepository, historyRepository,
        treeEventRepository, mock(AccountRepository.class), new ObjectMapper());
    when(settingRepository.findByEcclesiaUid(ECCLESIA)).thenReturn(Optional.empty());
    when(treeEventRepository.findByEcclesiaUid(ECCLESIA)).thenReturn(Optional.empty());
    when(historyRepository.findTop20ByEcclesiaUidOrderByChangedAtDesc(ECCLESIA))
        .thenReturn(List.of());
  }

  private static SettingRequest request(boolean enabled, int maxLength, List<Integer> days,
      ResponderVisibility responder, int threshold, int drawLimit) {
    return new SettingRequest(enabled, AuthorMode.AUTHOR_CHOICE, maxLength, days,
        WhoCanWrite.APPROVED_ONLY, ScopeMode.CHURCH_ONLY, responder, AdminSeesAnon.NEVER,
        ModerationMode.POST_REPORT, threshold, true, drawLimit, true);
  }

  @Test
  void 설정이_없는_교회는_가장_보수적인_기본값이다() {
    PrayerSetting s = service.getEffective(ECCLESIA);

    assertFalse(s.isEnabled());
    assertEquals(AuthorMode.AUTHOR_CHOICE, s.getAuthorMode());
    assertEquals(200, s.getMaxLength());
    assertEquals(List.of(7, 30), s.expiryDays());
    assertEquals(WhoCanWrite.APPROVED_ONLY, s.getWhoCanWrite());
    assertEquals(ScopeMode.CHURCH_ONLY, s.getScopeMode());
    assertEquals(ResponderVisibility.HIDDEN, s.getResponderVisibility());
    assertEquals(AdminSeesAnon.NEVER, s.getAdminSeesAnon());
    assertEquals(ModerationMode.POST_REPORT, s.getModerationMode());
    assertEquals(3, s.getAutoHideThreshold());
    assertEquals(5, s.getDailyDrawLimit());
  }

  @Test
  void 앱_설정은_꺼져_있으면_작성할_수_없다고_알린다() {
    AppSettingResponse response = service.appSetting(member(1L));

    assertFalse(response.enabled());
    assertFalse(response.canWrite());
  }

  @Test
  void 소속_교회가_없는_계정의_앱_설정은_꺼짐이다() {
    AppSettingResponse response = service.appSetting(
        com.gospelee.api.dto.account.AccountAuthDTO.builder().uid(1L).build());

    assertFalse(response.enabled());
    assertFalse(response.canWrite());
  }

  @Test
  void 관리자가_아니면_설정을_바꿀_수_없다() {
    assertThrows(AccessDeniedException.class, () -> service.updateSetting(member(1L),
        request(true, 200, List.of(7), ResponderVisibility.HIDDEN, 3, 5)));
    verify(settingRepository, never()).save(any());
  }

  @Test
  void 설정을_저장하면_바뀐_항목만_이력으로_남는다() {
    service.updateSetting(manager(9L),
        request(true, 200, List.of(30, 7), ResponderVisibility.NAMED, 3, 5));

    ArgumentCaptor<PrayerSetting> saved = ArgumentCaptor.forClass(PrayerSetting.class);
    verify(settingRepository).save(saved.capture());
    assertTrue(saved.getValue().isEnabled());
    // 노출 기간은 정렬된 문자열로 저장된다
    assertEquals("7,30", saved.getValue().getAllowedExpiryDays());

    ArgumentCaptor<PrayerSettingHistory> history = ArgumentCaptor.forClass(
        PrayerSettingHistory.class);
    verify(historyRepository).save(history.capture());
    assertEquals(9L, history.getValue().getChangedBy());
    assertTrue(history.getValue().getChanges().contains("\"enabled\""));
    assertTrue(history.getValue().getChanges().contains("\"responderVisibility\""));
    // 바꾸지 않은 항목은 이력에 없다
    assertFalse(history.getValue().getChanges().contains("\"maxLength\""));
  }

  @Test
  void 바뀐_것이_없으면_이력을_남기지_않는다() {
    PrayerSetting existing = PrayerSetting.defaults(ECCLESIA);
    when(settingRepository.findByEcclesiaUid(ECCLESIA)).thenReturn(Optional.of(existing));

    service.updateSetting(manager(9L),
        request(false, 200, List.of(7, 30), ResponderVisibility.HIDDEN, 3, 5));

    verify(historyRepository, never()).save(any());
  }

  @Test
  void 허용되지_않는_값은_거절한다() {
    assertThrows(IllegalArgumentException.class, () -> service.updateSetting(manager(9L),
        request(true, 10, List.of(7), ResponderVisibility.HIDDEN, 3, 5)));
    assertThrows(IllegalArgumentException.class, () -> service.updateSetting(manager(9L),
        request(true, 200, List.of(), ResponderVisibility.HIDDEN, 3, 5)));
    assertThrows(IllegalArgumentException.class, () -> service.updateSetting(manager(9L),
        request(true, 200, List.of(7), ResponderVisibility.HIDDEN, 0, 5)));
    assertThrows(IllegalArgumentException.class, () -> service.updateSetting(manager(9L),
        request(true, 200, List.of(7), ResponderVisibility.HIDDEN, 3, 99)));
    assertThrows(IllegalArgumentException.class, () -> service.updateSetting(manager(9L),
        request(true, 200, List.of(-1), ResponderVisibility.HIDDEN, 3, 5)));
    verify(settingRepository, never()).save(any());
  }

  @Test
  void 노출_기간은_중복을_빼고_정렬한다() {
    assertEquals("0,7,30", PrayerSettingService.normalizeExpiryDays(List.of(30, 0, 7, 7)));
  }

  @Test
  void 트리_이벤트는_종료일이_시작일보다_빠르면_거절한다() {
    LocalDateTime start = LocalDateTime.of(2026, 12, 1, 0, 0);
    assertThrows(IllegalArgumentException.class, () -> service.updateTreeEvent(manager(9L),
        new TreeEventRequest(true, start, start.minusDays(1), 10)));
  }
}
