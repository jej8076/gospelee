package com.gospelee.api.service.prayer;

import static com.gospelee.api.service.prayer.PrayerTestFixtures.ECCLESIA;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.card;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.enabledSetting;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.member;
import static com.gospelee.api.service.prayer.PrayerTestFixtures.setting;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.prayer.PrayerDTOs.CardCreateRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.DrawResponse;
import com.gospelee.api.dto.prayer.PrayerDTOs.MyCard;
import com.gospelee.api.dto.prayer.PrayerDTOs.ReportRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.RespondRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.RespondResponse;
import com.gospelee.api.dto.prayer.PrayerDTOs.SummaryResponse;
import com.gospelee.api.dto.prayer.PrayerDTOs.TreeResponse;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PrayerCard;
import com.gospelee.api.entity.PrayerCardResponse;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.entity.PrayerTreeEvent;
import com.gospelee.api.enums.PrayerEnums.AdminSeesAnon;
import com.gospelee.api.enums.PrayerEnums.AuthorMode;
import com.gospelee.api.enums.PrayerEnums.CardScope;
import com.gospelee.api.enums.PrayerEnums.CardStatus;
import com.gospelee.api.enums.PrayerEnums.DisplayMode;
import com.gospelee.api.enums.PrayerEnums.ModerationMode;
import com.gospelee.api.enums.PrayerEnums.ResponderVisibility;
import com.gospelee.api.enums.PrayerEnums.ScopeMode;
import com.gospelee.api.enums.PrayerEnums.WhoCanWrite;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.account.AccountBlockRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardDrawRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardReportRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardResponseRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupMemberRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

class PrayerCardServiceTest {

  private PrayerSettingService settingService;
  private PrayerCardRepository cardRepository;
  private PrayerCardResponseRepository responseRepository;
  private PrayerCardDrawRepository drawRepository;
  private PrayerCardReportRepository reportRepository;
  private PushGroupRepository pushGroupRepository;
  private PushGroupMemberRepository pushGroupMemberRepository;
  private AccountBlockRepository blockRepository;
  private AccountRepository accountRepository;
  private PrayerCardService service;

  private final AccountAuthDTO me = member(1L);

  @BeforeEach
  void setUp() {
    settingService = mock(PrayerSettingService.class);
    cardRepository = mock(PrayerCardRepository.class);
    responseRepository = mock(PrayerCardResponseRepository.class);
    drawRepository = mock(PrayerCardDrawRepository.class);
    reportRepository = mock(PrayerCardReportRepository.class);
    pushGroupRepository = mock(PushGroupRepository.class);
    pushGroupMemberRepository = mock(PushGroupMemberRepository.class);
    blockRepository = mock(AccountBlockRepository.class);
    accountRepository = mock(AccountRepository.class);
    service = new PrayerCardService(settingService, cardRepository, responseRepository,
        drawRepository, reportRepository, pushGroupRepository, pushGroupMemberRepository,
        blockRepository, accountRepository);

    when(settingService.getEffective(ECCLESIA)).thenReturn(enabledSetting());
    when(cardRepository.findByAuthorUid(anyLong())).thenReturn(List.of());
    when(cardRepository.save(any(PrayerCard.class))).thenAnswer(i -> i.getArgument(0));
  }

  private void use(PrayerSetting setting) {
    when(settingService.getEffective(ECCLESIA)).thenReturn(setting);
  }

  private static CardCreateRequest create(String content, DisplayMode mode, Integer days) {
    return new CardCreateRequest(content, mode, days, null, null);
  }

  // --- 접근 -------------------------------------------------------------------

  @Test
  void 기능이_꺼져_있으면_모든_성도용_기능을_거절한다() {
    use(PrayerSetting.defaults(ECCLESIA));

    assertThrows(AccessDeniedException.class,
        () -> service.create(me, create("기도", DisplayMode.ANON, 7)));
    assertThrows(AccessDeniedException.class, () -> service.draw(me));
    assertThrows(AccessDeniedException.class, () -> service.mine(me));
    assertThrows(AccessDeniedException.class, () -> service.tree(me));
    assertThrows(AccessDeniedException.class, () -> service.summary(me, null));
    assertThrows(AccessDeniedException.class,
        () -> service.respond(me, 5L, new RespondRequest(null, null)));
  }

  @Test
  void 가입이_승인되지_않은_성도는_기본_설정에서_쓸_수_없다() {
    AccountAuthDTO requesting = AccountAuthDTO.builder().uid(2L).ecclesiaUid(ECCLESIA)
        .ecclesiaStatus("REQ").leaveYn(Yn.N).build();

    assertThrows(AccessDeniedException.class, () -> service.draw(requesting));
  }

  @Test
  void 모두_허용으로_바꾸면_가입_요청_중인_성도도_쓸_수_있다() {
    use(setting(AuthorMode.AUTHOR_CHOICE, "7,30", WhoCanWrite.ALL_MEMBERS,
        ScopeMode.CHURCH_ONLY, ResponderVisibility.HIDDEN, AdminSeesAnon.NEVER,
        ModerationMode.POST_REPORT, 3, true, 5));
    AccountAuthDTO requesting = AccountAuthDTO.builder().uid(2L).ecclesiaUid(ECCLESIA)
        .ecclesiaStatus("REQ").leaveYn(Yn.N).build();
    when(cardRepository.findDrawCandidates(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of());

    assertNull(service.draw(requesting).card());
  }

  @Test
  void 탈퇴한_계정과_교회가_없는_계정은_쓸_수_없다() {
    AccountAuthDTO left = AccountAuthDTO.builder().uid(2L).ecclesiaUid(ECCLESIA)
        .ecclesiaStatus("APL").leaveYn(Yn.Y).build();

    assertThrows(AccessDeniedException.class, () -> service.draw(left));
    assertThrows(AccessDeniedException.class,
        () -> service.draw(AccountAuthDTO.builder().uid(3L).leaveYn(Yn.N).build()));
  }

  // --- 작성 -------------------------------------------------------------------

  @Test
  void 카드를_올리면_그때의_공개_정책이_카드에_박제된다() {
    use(setting(AuthorMode.AUTHOR_CHOICE, "7,30", WhoCanWrite.APPROVED_ONLY,
        ScopeMode.CHURCH_ONLY, ResponderVisibility.NAMED, AdminSeesAnon.ALWAYS,
        ModerationMode.POST_REPORT, 3, true, 5));

    service.create(me, create("  시험을 위해 기도해 주세요  ", DisplayMode.ANON, 30));

    ArgumentCaptor<PrayerCard> saved = ArgumentCaptor.forClass(PrayerCard.class);
    verify(cardRepository).save(saved.capture());
    PrayerCard card = saved.getValue();
    assertEquals(ResponderVisibility.NAMED, card.getResponderVisible());
    assertEquals(AdminSeesAnon.ALWAYS, card.getAdminSeesAnon());
    assertEquals("시험을 위해 기도해 주세요", card.getContent());
    assertEquals(CardStatus.ACTIVE, card.getStatus());
    assertEquals(CardScope.CHURCH, card.getScope());
    assertNull(card.getGroupId());
    assertEquals(1L, card.getAuthorUid());
    assertTrue(card.getExpiresAt().isAfter(LocalDateTime.now().plusDays(29)));
  }

  @Test
  void 사전_검수를_켠_교회의_카드는_승인_대기로_시작한다() {
    use(setting(AuthorMode.AUTHOR_CHOICE, "7", WhoCanWrite.APPROVED_ONLY,
        ScopeMode.CHURCH_ONLY, ResponderVisibility.HIDDEN, AdminSeesAnon.NEVER,
        ModerationMode.PRE_APPROVAL, 3, true, 5));

    MyCard card = service.create(me, create("기도", DisplayMode.ANON, 7));

    assertEquals(CardStatus.PENDING, card.status());
  }

  @Test
  void 작성_방식이_고정된_교회에서는_다른_방식을_거절한다() {
    use(setting(AuthorMode.ANON_ONLY, "7", WhoCanWrite.APPROVED_ONLY, ScopeMode.CHURCH_ONLY,
        ResponderVisibility.HIDDEN, AdminSeesAnon.NEVER, ModerationMode.POST_REPORT, 3, true, 5));
    assertThrows(IllegalArgumentException.class,
        () -> service.create(me, create("기도", DisplayMode.NAME, 7)));
    assertEquals(DisplayMode.ANON, service.create(me, create("기도", null, 7)).displayMode());

    use(setting(AuthorMode.NAME_ONLY, "7", WhoCanWrite.APPROVED_ONLY, ScopeMode.CHURCH_ONLY,
        ResponderVisibility.HIDDEN, AdminSeesAnon.NEVER, ModerationMode.POST_REPORT, 3, true, 5));
    assertThrows(IllegalArgumentException.class,
        () -> service.create(me, create("기도", DisplayMode.ANON, 7)));
    assertEquals(DisplayMode.NAME, service.create(me, create("기도", null, 7)).displayMode());
  }

  @Test
  void 작성자_선택이면_표시_방식을_반드시_골라야_한다() {
    assertThrows(IllegalArgumentException.class,
        () -> service.create(me, create("기도", null, 7)));
  }

  @Test
  void 빈_내용과_너무_긴_내용은_거절한다() {
    assertThrows(IllegalArgumentException.class,
        () -> service.create(me, create("   ", DisplayMode.ANON, 7)));
    assertThrows(IllegalArgumentException.class,
        () -> service.create(me, create("가".repeat(201), DisplayMode.ANON, 7)));
    verify(cardRepository, never()).save(any());
  }

  @Test
  void 허용되지_않은_노출_기간은_거절하고_0은_상시다() {
    assertThrows(IllegalArgumentException.class,
        () -> service.create(me, create("기도", DisplayMode.ANON, 90)));

    use(setting(AuthorMode.AUTHOR_CHOICE, "0,7", WhoCanWrite.APPROVED_ONLY,
        ScopeMode.CHURCH_ONLY, ResponderVisibility.HIDDEN, AdminSeesAnon.NEVER,
        ModerationMode.POST_REPORT, 3, true, 5));
    assertNull(service.create(me, create("기도", DisplayMode.ANON, 0)).expiresAt());
  }

  @Test
  void 그룹_공개를_허용하지_않은_교회에서는_그룹_카드를_거절한다() {
    assertThrows(IllegalArgumentException.class, () -> service.create(me,
        new CardCreateRequest("기도", DisplayMode.ANON, 7, CardScope.GROUP, 3L)));
  }

  @Test
  void 내가_속하지_않은_그룹에는_올릴_수_없다() {
    use(setting(AuthorMode.AUTHOR_CHOICE, "7", WhoCanWrite.APPROVED_ONLY,
        ScopeMode.GROUP_ALLOWED, ResponderVisibility.HIDDEN, AdminSeesAnon.NEVER,
        ModerationMode.POST_REPORT, 3, true, 5));
    when(pushGroupRepository.findByIdAndEcclesiaUid(3L, ECCLESIA))
        .thenReturn(Optional.of(mock(com.gospelee.api.entity.PushGroup.class)));
    when(pushGroupMemberRepository.existsByGroupIdAndAccountUid(3L, 1L)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> service.create(me,
        new CardCreateRequest("기도", DisplayMode.ANON, 7, CardScope.GROUP, 3L)));

    when(pushGroupMemberRepository.existsByGroupIdAndAccountUid(3L, 1L)).thenReturn(true);
    MyCard created = service.create(me,
        new CardCreateRequest("기도", DisplayMode.ANON, 7, CardScope.GROUP, 3L));
    assertEquals(CardScope.GROUP, created.scope());
  }

  @Test
  void 한_사람이_동시에_올릴_수_있는_카드_수에는_한도가_있다() {
    List<PrayerCard> open = java.util.stream.IntStream.range(0, 5)
        .mapToObj(i -> card(100L + i, 1L, 0)).toList();
    when(cardRepository.findByAuthorUid(1L)).thenReturn(open);

    assertThrows(IllegalArgumentException.class,
        () -> service.create(me, create("기도", DisplayMode.ANON, 7)));
  }

  // --- 뽑기 -------------------------------------------------------------------

  @Test
  void 하루_뽑기_횟수를_넘으면_거절한다() {
    when(drawRepository.countByDrawerUidAndDrawnAtGreaterThanEqual(anyLong(), any()))
        .thenReturn(5L);

    assertThrows(IllegalArgumentException.class, () -> service.draw(me));
    verify(drawRepository, never()).save(any());
  }

  @Test
  void 뽑을_카드가_없으면_카드_없이_남은_횟수만_알려_준다() {
    when(cardRepository.findDrawCandidates(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of());

    DrawResponse response = service.draw(me);

    assertNull(response.card());
    assertEquals(5, response.remainingDraws());
  }

  @Test
  void 뽑기는_기도를_가장_덜_받은_카드_중에서만_고른다() {
    List<PrayerCard> sorted = List.of(card(1L, 50L, 0), card(2L, 51L, 0), card(3L, 52L, 4));
    when(cardRepository.findDrawCandidates(any(), any(), any(), any(), any(), any()))
        .thenReturn(sorted);

    for (int i = 0; i < 60; i++) {
      DrawResponse response = service.draw(me);
      assertTrue(response.card().id() == 1L || response.card().id() == 2L);
    }
  }

  @Test
  void 뽑은_카드에는_기도자_공개_정책_안내가_담기고_익명이면_작성자_이름이_없다() {
    when(cardRepository.findDrawCandidates(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(card(1L, 50L, 0)));

    DrawResponse response = service.draw(me);

    assertNull(response.card().authorName());
    assertEquals(PrayerCardService.responderNotice(ResponderVisibility.HIDDEN),
        response.card().responderNotice());
    assertEquals(4, response.remainingDraws());
    verify(accountRepository, never()).findById(anyLong());
  }

  @Test
  void 이름을_공개한_카드에는_작성자_이름이_담긴다() {
    PrayerCard named = card(1L, 50L, DisplayMode.NAME, ResponderVisibility.HIDDEN,
        AdminSeesAnon.NEVER, 0);
    Account author = mock(Account.class);
    when(author.getName()).thenReturn("홍길동");
    when(accountRepository.findById(50L)).thenReturn(Optional.of(author));
    when(cardRepository.findDrawCandidates(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(named));

    assertEquals("홍길동", service.draw(me).card().authorName());
  }

  // --- 기도하기 ---------------------------------------------------------------

  private RespondResponse respondTo(PrayerCard card, RespondRequest request) {
    when(cardRepository.findByIdAndEcclesiaUid(card.getId(), ECCLESIA))
        .thenReturn(Optional.of(card));
    return service.respond(me, card.getId(), request);
  }

  private PrayerCardResponse savedResponse() {
    ArgumentCaptor<PrayerCardResponse> captor = ArgumentCaptor.forClass(
        PrayerCardResponse.class);
    verify(responseRepository).saveAndFlush(captor.capture());
    return captor.getValue();
  }

  @Test
  void 기도하면_기도_수가_늘고_다시_눌러도_한_번만_센다() {
    PrayerCard card = card(5L, 50L, 2);

    RespondResponse first = respondTo(card, new RespondRequest(null, null));
    assertEquals(3, first.responseCount());
    assertFalse(first.alreadyResponded());
    verify(cardRepository).increaseResponseCount(5L);

    when(responseRepository.existsByCardIdAndResponderUid(5L, 1L)).thenReturn(true);
    RespondResponse second = service.respond(me, 5L, new RespondRequest(null, null));
    assertTrue(second.alreadyResponded());
    verify(responseRepository).saveAndFlush(any());
  }

  @Test
  void 동시에_눌러_중복_저장이_거절되어도_한_번만_센다() {
    PrayerCard card = card(5L, 50L, 2);
    when(responseRepository.saveAndFlush(any(PrayerCardResponse.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    RespondResponse response = respondTo(card, new RespondRequest(null, null));

    assertTrue(response.alreadyResponded());
    verify(cardRepository, never()).increaseResponseCount(anyLong());
  }

  @Test
  void 기도자_비공개_카드는_이름_공개를_요청해도_공개되지_않는다() {
    respondTo(card(5L, 50L, 0), new RespondRequest(true, null));

    assertFalse(savedResponse().isShowName());
  }

  @Test
  void 이름_공개_정책_카드는_요청과_상관없이_이름이_공개된다() {
    respondTo(card(5L, 50L, DisplayMode.ANON, ResponderVisibility.NAMED, AdminSeesAnon.NEVER, 0),
        new RespondRequest(false, null));

    assertTrue(savedResponse().isShowName());
  }

  @Test
  void 기도자_선택_정책_카드는_요청을_따른다() {
    respondTo(card(5L, 50L, DisplayMode.ANON, ResponderVisibility.RESPONDER_CHOICE,
        AdminSeesAnon.NEVER, 0), new RespondRequest(true, null));

    assertTrue(savedResponse().isShowName());
  }

  @Test
  void 응원_문구는_정해진_것만_받고_끈_교회에서는_거절한다() {
    PrayerCard card = card(5L, 50L, 0);
    when(cardRepository.findByIdAndEcclesiaUid(5L, ECCLESIA)).thenReturn(Optional.of(card));

    assertThrows(IllegalArgumentException.class,
        () -> service.respond(me, 5L, new RespondRequest(null, "ANYTHING")));

    use(setting(AuthorMode.AUTHOR_CHOICE, "7", WhoCanWrite.APPROVED_ONLY,
        ScopeMode.CHURCH_ONLY, ResponderVisibility.HIDDEN, AdminSeesAnon.NEVER,
        ModerationMode.POST_REPORT, 3, false, 5));
    assertThrows(IllegalArgumentException.class,
        () -> service.respond(me, 5L, new RespondRequest(null, "PEACE")));
  }

  @Test
  void 내_기도제목에는_기도했어요를_남길_수_없다() {
    PrayerCard mine = card(5L, 1L, 0);
    when(cardRepository.findByIdAndEcclesiaUid(5L, ECCLESIA)).thenReturn(Optional.of(mine));

    assertThrows(IllegalArgumentException.class,
        () -> service.respond(me, 5L, new RespondRequest(null, null)));
  }

  @Test
  void 다른_교회_카드와_숨김_카드와_차단한_사람의_카드는_없는_것처럼_취급한다() {
    when(cardRepository.findByIdAndEcclesiaUid(9L, ECCLESIA)).thenReturn(Optional.empty());
    assertThrows(java.util.NoSuchElementException.class, () -> service.get(me, 9L));

    PrayerCard hidden = card(5L, 50L, 0);
    hidden.changeStatus(CardStatus.HIDDEN);
    when(cardRepository.findByIdAndEcclesiaUid(5L, ECCLESIA)).thenReturn(Optional.of(hidden));
    assertThrows(java.util.NoSuchElementException.class, () -> service.get(me, 5L));

    PrayerCard blocked = card(6L, 60L, 0);
    when(cardRepository.findByIdAndEcclesiaUid(6L, ECCLESIA)).thenReturn(Optional.of(blocked));
    when(blockRepository.existsByBlockerUidAndBlockedUid(1L, 60L)).thenReturn(true);
    assertThrows(java.util.NoSuchElementException.class, () -> service.get(me, 6L));
  }

  @Test
  void 내가_속하지_않은_그룹의_카드는_볼_수_없다() {
    PrayerCard groupCard = PrayerCard.builder().id(8L).ecclesiaUid(ECCLESIA).authorUid(50L)
        .content("기도").displayMode(DisplayMode.ANON).scope(CardScope.GROUP).groupId(3L)
        .status(CardStatus.ACTIVE).responderVisible(ResponderVisibility.HIDDEN)
        .adminSeesAnon(AdminSeesAnon.NEVER).build();
    when(cardRepository.findByIdAndEcclesiaUid(8L, ECCLESIA)).thenReturn(Optional.of(groupCard));
    when(pushGroupMemberRepository.existsByGroupIdAndAccountUid(3L, 1L)).thenReturn(false);

    assertThrows(java.util.NoSuchElementException.class, () -> service.get(me, 8L));
  }

  // --- 내 카드 ----------------------------------------------------------------

  private PrayerCardResponse response(long responder, boolean showName, String cheer) {
    return new PrayerCardResponse(5L, responder, showName, cheer, LocalDateTime.now());
  }

  private Account account(long uid, String name) {
    Account account = mock(Account.class);
    when(account.getUid()).thenReturn(uid);
    when(account.getName()).thenReturn(name);
    return account;
  }

  @Test
  void 기도자_비공개_카드는_기도한_사람의_이름을_알려_주지_않는다() {
    PrayerCard card = card(5L, 1L, 2);
    when(cardRepository.findByAuthorUidOrderByIdDesc(1L)).thenReturn(List.of(card));
    when(responseRepository.findByCardId(5L)).thenReturn(
        List.of(response(10L, true, "PEACE"), response(11L, false, "PEACE")));

    MyCard mine = service.mine(me).get(0);

    assertEquals(2, mine.responseCount());
    assertTrue(mine.responders().isEmpty());
    assertEquals(2, mine.cheers().get("PEACE"));
    verify(accountRepository, never()).findAllById(any());
  }

  @Test
  void 기도자_선택_카드는_이름을_공개하겠다고_한_사람만_보여_준다() {
    PrayerCard card = card(5L, 1L, DisplayMode.ANON, ResponderVisibility.RESPONDER_CHOICE,
        AdminSeesAnon.NEVER, 2);
    when(cardRepository.findByAuthorUidOrderByIdDesc(1L)).thenReturn(List.of(card));
    when(responseRepository.findByCardId(5L)).thenReturn(
        List.of(response(10L, true, null), response(11L, false, null)));
    Account kim = account(10L, "김기도");
    when(accountRepository.findAllById(List.of(10L))).thenReturn(List.of(kim));

    assertEquals(List.of("김기도"), service.mine(me).get(0).responders());
  }

  @Test
  void 이름_공개_카드는_기도한_모든_사람의_이름을_보여_준다() {
    PrayerCard card = card(5L, 1L, DisplayMode.ANON, ResponderVisibility.NAMED,
        AdminSeesAnon.NEVER, 2);
    when(cardRepository.findByAuthorUidOrderByIdDesc(1L)).thenReturn(List.of(card));
    when(responseRepository.findByCardId(5L)).thenReturn(
        List.of(response(10L, false, null), response(11L, false, null)));
    Account kim = account(10L, "김기도");
    Account lee = account(11L, "이은혜");
    when(accountRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(kim, lee));

    assertEquals(List.of("김기도", "이은혜"), service.mine(me).get(0).responders());
  }

  @Test
  void 내_카드가_아니면_응답됨_표시와_삭제를_할_수_없다() {
    PrayerCard other = card(5L, 50L, 0);
    when(cardRepository.findByIdAndEcclesiaUid(5L, ECCLESIA)).thenReturn(Optional.of(other));

    assertThrows(java.util.NoSuchElementException.class, () -> service.markAnswered(me, 5L));
    assertThrows(java.util.NoSuchElementException.class, () -> service.delete(me, 5L));
    verify(cardRepository, never()).deleteAll(any());
  }

  @Test
  void 카드를_지우면_딸린_기도와_신고와_뽑기_기록도_지운다() {
    PrayerCard mine = card(5L, 1L, 1);
    when(cardRepository.findByIdAndEcclesiaUid(5L, ECCLESIA)).thenReturn(Optional.of(mine));

    service.delete(me, 5L);

    verify(responseRepository).deleteByCardIdIn(List.of(5L));
    verify(drawRepository).deleteByCardIdIn(List.of(5L));
    verify(reportRepository).deleteByCardIdIn(List.of(5L));
    verify(cardRepository).deleteAll(List.of(mine));
  }

  @Test
  void 응답됨으로_표시하면_상태가_바뀐다() {
    PrayerCard mine = card(5L, 1L, 1);
    when(cardRepository.findByIdAndEcclesiaUid(5L, ECCLESIA)).thenReturn(Optional.of(mine));

    service.markAnswered(me, 5L);

    assertEquals(CardStatus.ANSWERED, mine.getStatus());
  }

  // --- 신고 -------------------------------------------------------------------

  private void report(PrayerCard card) {
    when(cardRepository.findByIdAndEcclesiaUid(card.getId(), ECCLESIA))
        .thenReturn(Optional.of(card));
    service.report(me, card.getId(), new ReportRequest("SPAM", null));
  }

  @Test
  void 신고가_설정한_수에_닿으면_카드가_숨겨진다() {
    PrayerCard card = card(5L, 50L, 0);
    ReflectionTestUtils.setField(card, "reportCount", 2);

    report(card);

    assertEquals(3, card.getReportCount());
    assertEquals(CardStatus.HIDDEN, card.getStatus());
  }

  @Test
  void 신고가_모자라면_카드는_그대로_보인다() {
    PrayerCard card = card(5L, 50L, 0);

    report(card);

    assertEquals(1, card.getReportCount());
    assertEquals(CardStatus.ACTIVE, card.getStatus());
  }

  @Test
  void 같은_사람의_중복_신고는_무시한다() {
    PrayerCard card = card(5L, 50L, 0);
    when(reportRepository.existsByCardIdAndReporterUid(5L, 1L)).thenReturn(true);

    report(card);

    assertEquals(0, card.getReportCount());
    verify(reportRepository, never()).save(any());
  }

  @Test
  void 내_기도제목은_신고할_수_없다() {
    PrayerCard mine = card(5L, 1L, 0);
    when(cardRepository.findByIdAndEcclesiaUid(5L, ECCLESIA)).thenReturn(Optional.of(mine));

    assertThrows(IllegalArgumentException.class,
        () -> service.report(me, 5L, new ReportRequest("SPAM", null)));
  }

  // --- 트리 -------------------------------------------------------------------

  @Test
  void 점등률은_카드가_최소_기준에_못_미치면_보여_주지_않는다() {
    assertNull(PrayerCardService.progress(5, 9, 10));
    assertNull(PrayerCardService.progress(0, 0, 1));
    assertEquals(50, PrayerCardService.progress(5, 10, 10));
    assertEquals(100, PrayerCardService.progress(10, 10, 10));
  }

  @Test
  void 이벤트가_꺼져_있거나_기간이_아니면_트리를_거절한다() {
    when(settingService.getTreeEvent(ECCLESIA)).thenReturn(new PrayerTreeEvent(ECCLESIA));
    assertThrows(IllegalArgumentException.class, () -> service.tree(me));

    PrayerTreeEvent past = new PrayerTreeEvent(ECCLESIA);
    past.apply(true, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1), 10);
    when(settingService.getTreeEvent(ECCLESIA)).thenReturn(past);
    assertThrows(IllegalArgumentException.class, () -> service.tree(me));
  }

  @Test
  void 트리는_카드마다_슬롯과_점등_여부를_내려_준다() {
    PrayerTreeEvent running = new PrayerTreeEvent(ECCLESIA);
    running.apply(true, null, null, 2);
    when(settingService.getTreeEvent(ECCLESIA)).thenReturn(running);
    when(cardRepository.findTreeCards(any(), any(), any(), any(), any()))
        .thenReturn(List.of(card(1L, 50L, 3), card(2L, 51L, 0)));
    when(cardRepository.countTreeCards(any(), any(), any(), any())).thenReturn(2L);
    when(cardRepository.countLitTreeCards(any(), any(), any(), any())).thenReturn(1L);

    TreeResponse tree = service.tree(me);

    assertEquals(2, tree.cards().size());
    assertEquals(0, tree.cards().get(0).slot());
    assertTrue(tree.cards().get(0).lit());
    assertFalse(tree.cards().get(1).lit());
    assertEquals(50, tree.progress());
  }

  // --- 탈퇴 -------------------------------------------------------------------

  @Test
  void 탈퇴하면_내_카드와_내가_남긴_기록을_모두_지운다() {
    PrayerCard mine = card(5L, 1L, 1);
    when(cardRepository.findByAuthorUid(1L)).thenReturn(List.of(mine));

    service.removeAllByAccount(1L);

    verify(responseRepository).deleteByCardIdIn(List.of(5L));
    verify(cardRepository).deleteAll(List.of(mine));
    verify(responseRepository).deleteByResponderUid(1L);
    verify(reportRepository).deleteByReporterUid(1L);
    verify(drawRepository).deleteByDrawerUid(1L);
  }

  // --- 배너 요약 ----------------------------------------------------------------

  @Test
  void 요약은_뽑을_수_있는_수와_오늘_남은_횟수와_새_기도_수를_내려_준다() {
    LocalDateTime since = LocalDateTime.now().minusDays(1);
    when(cardRepository.countDrawable(any(), any(), any(), any(), any())).thenReturn(7L);
    when(drawRepository.countByDrawerUidAndDrawnAtGreaterThanEqual(anyLong(), any()))
        .thenReturn(2L);
    when(responseRepository.countNewOnMyCards(1L, since)).thenReturn(3L);
    when(settingService.getTreeEvent(ECCLESIA)).thenReturn(new PrayerTreeEvent(ECCLESIA));

    SummaryResponse summary = service.summary(me, since);

    assertEquals(7L, summary.availableCards());
    assertEquals(3, summary.remainingDraws());
    assertEquals(3L, summary.newResponses());
    assertFalse(summary.treeRunning());
    assertNull(summary.treeProgress());
  }

  @Test
  void 요약은_기준_시각이_없으면_새_기도를_세지_않는다() {
    when(settingService.getTreeEvent(ECCLESIA)).thenReturn(new PrayerTreeEvent(ECCLESIA));

    SummaryResponse summary = service.summary(me, null);

    assertEquals(0L, summary.newResponses());
    verify(responseRepository, never()).countNewOnMyCards(anyLong(), any());
  }
}
