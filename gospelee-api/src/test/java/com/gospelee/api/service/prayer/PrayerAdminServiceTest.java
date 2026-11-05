package com.gospelee.api.service.prayer;

import static com.gospelee.api.service.prayer.PrayerTestFixtures.ECCLESIA;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.card;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.manager;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.member;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.prayer.PrayerDTOs.AdminCardItem;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PrayerCard;
import com.gospelee.api.entity.PrayerCardReport;
import com.gospelee.api.enums.PrayerEnums.AdminSeesAnon;
import com.gospelee.api.enums.PrayerEnums.CardStatus;
import com.gospelee.api.enums.PrayerEnums.DisplayMode;
import com.gospelee.api.enums.PrayerEnums.ReportStatus;
import com.gospelee.api.enums.PrayerEnums.ResponderVisibility;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardReportRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class PrayerAdminServiceTest {

  private PrayerCardRepository cardRepository;
  private PrayerCardReportRepository reportRepository;
  private AccountRepository accountRepository;
  private PrayerAdminService service;

  @BeforeEach
  void setUp() {
    cardRepository = mock(PrayerCardRepository.class);
    reportRepository = mock(PrayerCardReportRepository.class);
    accountRepository = mock(AccountRepository.class);
    service = new PrayerAdminService(cardRepository, reportRepository, accountRepository);
  }

  private static PrayerCard anon(AdminSeesAnon policy) {
    return card(1L, 50L, DisplayMode.ANON, ResponderVisibility.HIDDEN, policy, 0);
  }

  @Test
  void 이름을_공개한_카드는_작성자가_항상_보인다() {
    PrayerCard named = card(1L, 50L, DisplayMode.NAME, ResponderVisibility.HIDDEN,
        AdminSeesAnon.NEVER, 0);

    assertTrue(PrayerAdminService.authorVisibleToAdmin(named, false));
  }

  @Test
  void 익명_카드의_작성자는_카드에_박제된_정책이_허용할_때만_보인다() {
    assertFalse(PrayerAdminService.authorVisibleToAdmin(anon(AdminSeesAnon.NEVER), true));
    assertFalse(PrayerAdminService.authorVisibleToAdmin(anon(AdminSeesAnon.NEVER), false));
    assertTrue(PrayerAdminService.authorVisibleToAdmin(anon(AdminSeesAnon.REPORTED_ONLY), true));
    assertFalse(
        PrayerAdminService.authorVisibleToAdmin(anon(AdminSeesAnon.REPORTED_ONLY), false));
    assertTrue(PrayerAdminService.authorVisibleToAdmin(anon(AdminSeesAnon.ALWAYS), false));
  }

  @Test
  void 신고_목록은_정책이_숨기는_익명_작성자를_내려_주지_않는다() {
    PrayerCard hidden = anon(AdminSeesAnon.NEVER);
    when(cardRepository.findWithPendingReports(ECCLESIA, ReportStatus.PENDING))
        .thenReturn(List.of(hidden));
    when(reportRepository.findByCardIdIn(List.of(1L))).thenReturn(List.of(
        new PrayerCardReport(1L, 2L, "SPAM", null, LocalDateTime.now())));

    List<AdminCardItem> items = service.reportedCards(manager(9L));

    assertEquals(1, items.size());
    assertNull(items.get(0).authorName());
    assertEquals(List.of("SPAM"), items.get(0).reasons());
  }

  @Test
  void 신고_목록은_정책이_허용하면_익명_작성자도_보여_준다() {
    PrayerCard reported = anon(AdminSeesAnon.REPORTED_ONLY);
    Account author = mock(Account.class);
    when(author.getUid()).thenReturn(50L);
    when(author.getName()).thenReturn("홍길동");
    when(cardRepository.findWithPendingReports(ECCLESIA, ReportStatus.PENDING))
        .thenReturn(List.of(reported));
    when(reportRepository.findByCardIdIn(List.of(1L))).thenReturn(List.of());
    when(accountRepository.findAllById(List.of(50L))).thenReturn(List.of(author));

    assertEquals("홍길동", service.reportedCards(manager(9L)).get(0).authorName());
  }

  @Test
  void 카드를_내리면_대기_신고가_내림으로_처리된다() {
    PrayerCard card = anon(AdminSeesAnon.NEVER);
    PrayerCardReport report = new PrayerCardReport(1L, 2L, "SPAM", null, LocalDateTime.now());
    when(cardRepository.findByIdAndEcclesiaUid(1L, ECCLESIA)).thenReturn(Optional.of(card));
    when(reportRepository.findByCardIdAndStatus(1L, ReportStatus.PENDING))
        .thenReturn(List.of(report));

    service.hide(manager(9L), 1L);

    assertEquals(CardStatus.HIDDEN, card.getStatus());
    assertEquals(ReportStatus.RESOLVED, report.getStatus());
  }

  @Test
  void 숨김을_풀면_대기_신고가_문제없음으로_처리된다() {
    PrayerCard card = anon(AdminSeesAnon.NEVER);
    card.changeStatus(CardStatus.HIDDEN);
    PrayerCardReport report = new PrayerCardReport(1L, 2L, "SPAM", null, LocalDateTime.now());
    when(cardRepository.findByIdAndEcclesiaUid(1L, ECCLESIA)).thenReturn(Optional.of(card));
    when(reportRepository.findByCardIdAndStatus(1L, ReportStatus.PENDING))
        .thenReturn(List.of(report));

    service.restore(manager(9L), 1L);

    assertEquals(CardStatus.ACTIVE, card.getStatus());
    assertEquals(ReportStatus.DISMISSED, report.getStatus());
  }

  @Test
  void 승인은_대기_중인_카드에만_할_수_있다() {
    PrayerCard pending = anon(AdminSeesAnon.NEVER);
    pending.changeStatus(CardStatus.PENDING);
    PrayerCard active = anon(AdminSeesAnon.NEVER);
    when(cardRepository.findByIdAndEcclesiaUid(1L, ECCLESIA)).thenReturn(Optional.of(pending));
    service.approve(manager(9L), 1L);
    assertEquals(CardStatus.ACTIVE, pending.getStatus());

    when(cardRepository.findByIdAndEcclesiaUid(1L, ECCLESIA)).thenReturn(Optional.of(active));
    assertThrows(IllegalArgumentException.class, () -> service.approve(manager(9L), 1L));
  }

  @Test
  void 관리자가_아니면_어떤_관리_기능도_쓸_수_없다() {
    assertThrows(AccessDeniedException.class, () -> service.reportedCards(member(1L)));
    assertThrows(AccessDeniedException.class, () -> service.pendingCards(member(1L)));
    assertThrows(AccessDeniedException.class, () -> service.hide(member(1L), 1L));
    assertThrows(AccessDeniedException.class, () -> service.stats(member(1L)));
  }
}
