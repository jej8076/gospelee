package com.gospelee.api.service.prayer;

import static com.gospelee.api.service.prayer.PrayerTestFixtures.ECCLESIA;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.card;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.enabledSetting;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PrayerCardResponse;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardResponseRepository;
import com.gospelee.api.service.FirebaseService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrayerSummaryNotifierTest {

  private final LocalDateTime today = LocalDateTime.of(2026, 12, 1, 0, 0);

  private PrayerCardResponseRepository responseRepository;
  private PrayerCardRepository cardRepository;
  private AccountRepository accountRepository;
  private PrayerSettingService settingService;
  private FirebaseService firebaseService;
  private PrayerSummaryNotifier notifier;

  @BeforeEach
  void setUp() throws Exception {
    responseRepository = mock(PrayerCardResponseRepository.class);
    cardRepository = mock(PrayerCardRepository.class);
    accountRepository = mock(AccountRepository.class);
    settingService = mock(PrayerSettingService.class);
    firebaseService = mock(FirebaseService.class);
    notifier = new PrayerSummaryNotifier(responseRepository, cardRepository, accountRepository,
        settingService, firebaseService);

    when(responseRepository.findByInsertTimeGreaterThanEqualAndInsertTimeLessThan(
        today.minusDays(1), today)).thenReturn(List.of(
        new PrayerCardResponse(5L, 10L, false, null, today.minusHours(5)),
        new PrayerCardResponse(5L, 11L, false, null, today.minusHours(3)),
        new PrayerCardResponse(6L, 10L, false, null, today.minusHours(2))));
    when(cardRepository.findAllById(any())).thenReturn(
        List.of(card(5L, 50L, 2), card(6L, 50L, 1)));
    Account author = mock(Account.class);
    when(author.getPushToken()).thenReturn("token-50");
    when(accountRepository.findById(50L)).thenReturn(Optional.of(author));
  }

  @Test
  void 어제_받은_기도를_작성자별로_모아_한_번만_알린다() throws Exception {
    when(settingService.getEffective(ECCLESIA)).thenReturn(enabledSetting());

    notifier.sendSummary(today);

    verify(firebaseService).sendNotification(eq("token-50"), eq("기도 쪽지"),
        eq("어제 3번 기도해 주셨어요."), anyMap());
  }

  @Test
  void 요약_푸시를_끈_교회에는_보내지_않는다() throws Exception {
    PrayerSetting off = PrayerSetting.defaults(ECCLESIA);
    off.apply(true, off.getAuthorMode(), 200, "7,30", off.getWhoCanWrite(), off.getScopeMode(),
        off.getResponderVisibility(), off.getAdminSeesAnon(), off.getModerationMode(), 3, true, 5,
        false);
    when(settingService.getEffective(ECCLESIA)).thenReturn(off);

    notifier.sendSummary(today);

    verify(firebaseService, never()).sendNotification(any(), any(), any(), anyMap());
  }

  @Test
  void 기능을_끈_교회에는_보내지_않는다() throws Exception {
    when(settingService.getEffective(ECCLESIA)).thenReturn(PrayerSetting.defaults(ECCLESIA));

    notifier.sendSummary(today);

    verify(firebaseService, never()).sendNotification(any(), any(), any(), anyMap());
  }
}
