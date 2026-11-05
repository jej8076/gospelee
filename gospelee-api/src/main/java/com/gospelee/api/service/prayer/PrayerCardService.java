package com.gospelee.api.service.prayer;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.prayer.PrayerDTOs.CardCreateRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.CardView;
import com.gospelee.api.dto.prayer.PrayerDTOs.DrawResponse;
import com.gospelee.api.dto.prayer.PrayerDTOs.MyCard;
import com.gospelee.api.dto.prayer.PrayerDTOs.ReportRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.RespondRequest;
import com.gospelee.api.dto.prayer.PrayerDTOs.RespondResponse;
import com.gospelee.api.dto.prayer.PrayerDTOs.TreeCard;
import com.gospelee.api.dto.prayer.PrayerDTOs.TreeResponse;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PrayerCard;
import com.gospelee.api.entity.PrayerCardDraw;
import com.gospelee.api.entity.PrayerCardReport;
import com.gospelee.api.entity.PrayerCardResponse;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.entity.PrayerTreeEvent;
import com.gospelee.api.enums.PrayerEnums.AuthorMode;
import com.gospelee.api.enums.PrayerEnums.CardScope;
import com.gospelee.api.enums.PrayerEnums.CardStatus;
import com.gospelee.api.enums.PrayerEnums.Cheer;
import com.gospelee.api.enums.PrayerEnums.DisplayMode;
import com.gospelee.api.enums.PrayerEnums.ModerationMode;
import com.gospelee.api.enums.PrayerEnums.ResponderVisibility;
import com.gospelee.api.enums.PrayerEnums.ScopeMode;
import com.gospelee.api.repository.jpa.account.AccountBlockRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardDrawRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardReportRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardResponseRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupMemberRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 기도 쪽지 작성, 뽑기, 기도하기, 신고, 트리. 모든 메서드는 컨트롤러가 넘긴 로그인 계정의 교회로 한정되며,
 * 교회 설정이 꺼져 있으면 거절한다 (서버가 최종 판단).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrayerCardService {

  /** 한 사람이 동시에 올려 둘 수 있는 카드 수 (도배 방지) */
  static final int MAX_OPEN_CARDS_PER_AUTHOR = 5;
  /** 뽑기 후보로 한 번에 읽어 오는 카드 수 */
  static final int DRAW_POOL_SIZE = 30;
  /** 트리에 걸리는 카드 수 (장식 슬롯 수) */
  static final int TREE_SLOTS = 40;

  private final PrayerSettingService settingService;
  private final PrayerCardRepository cardRepository;
  private final PrayerCardResponseRepository responseRepository;
  private final PrayerCardDrawRepository drawRepository;
  private final PrayerCardReportRepository reportRepository;
  private final PushGroupRepository pushGroupRepository;
  private final PushGroupMemberRepository pushGroupMemberRepository;
  private final AccountBlockRepository blockRepository;
  private final AccountRepository accountRepository;

  private final Random random = new Random();

  // ---------------------------------------------------------------------------
  // 작성과 내 카드
  // ---------------------------------------------------------------------------

  @Transactional
  public MyCard create(AccountAuthDTO account, CardCreateRequest request) {
    PrayerSetting setting = settingService.getEffective(account.getEcclesiaUid());
    PrayerAccess.requireParticipant(account, setting);
    LocalDateTime now = LocalDateTime.now();

    String content = request.content() == null ? "" : request.content().trim();
    if (content.isEmpty()) {
      throw new IllegalArgumentException("기도제목을 입력해 주세요.");
    }
    if (content.length() > setting.getMaxLength()) {
      throw new IllegalArgumentException(
          "기도제목은 " + setting.getMaxLength() + "자까지 적을 수 있습니다.");
    }

    DisplayMode displayMode = resolveDisplayMode(setting.getAuthorMode(), request.displayMode());
    LocalDateTime expiresAt = resolveExpiry(setting, request.expiryDays(), now);

    CardScope scope = request.scope() == null ? CardScope.CHURCH : request.scope();
    Long groupId = null;
    if (scope == CardScope.GROUP) {
      groupId = validateGroup(account, setting, request.groupId());
    }

    long open = cardRepository.findByAuthorUid(account.getUid()).stream()
        .filter(c -> (c.getStatus() == CardStatus.ACTIVE || c.getStatus() == CardStatus.PENDING)
            && !c.isExpired(now))
        .count();
    if (open >= MAX_OPEN_CARDS_PER_AUTHOR) {
      throw new IllegalArgumentException(
          "기도제목은 동시에 " + MAX_OPEN_CARDS_PER_AUTHOR + "개까지 올릴 수 있어요. 기존 기도제목을 내리거나 기간이 지난 뒤에 올려 주세요.");
    }

    // 카드를 올린 시점의 공개 정책을 카드에 복사해 둔다. 이후 설정이 바뀌어도 이 카드에는 적용하지 않는다
    PrayerCard card = cardRepository.save(PrayerCard.builder()
        .ecclesiaUid(account.getEcclesiaUid())
        .authorUid(account.getUid())
        .content(content)
        .displayMode(displayMode)
        .scope(scope)
        .groupId(groupId)
        .expiresAt(expiresAt)
        .status(setting.getModerationMode() == ModerationMode.PRE_APPROVAL
            ? CardStatus.PENDING : CardStatus.ACTIVE)
        .responderVisible(setting.getResponderVisibility())
        .adminSeesAnon(setting.getAdminSeesAnon())
        .build());
    return toMyCard(card, List.of(), now);
  }

  @Transactional(readOnly = true)
  public List<MyCard> mine(AccountAuthDTO account) {
    PrayerAccess.requireParticipant(account,
        settingService.getEffective(account.getEcclesiaUid()));
    LocalDateTime now = LocalDateTime.now();
    List<PrayerCard> cards = cardRepository.findByAuthorUidOrderByIdDesc(account.getUid());

    List<MyCard> result = new ArrayList<>();
    for (PrayerCard card : cards) {
      result.add(toMyCard(card, responseRepository.findByCardId(card.getId()), now));
    }
    return result;
  }

  @Transactional
  public void markAnswered(AccountAuthDTO account, Long cardId) {
    PrayerCard card = requireMine(account, cardId);
    if (card.getStatus() != CardStatus.ACTIVE && card.getStatus() != CardStatus.ANSWERED) {
      throw new IllegalArgumentException("노출 중인 기도제목만 응답됨으로 표시할 수 있습니다.");
    }
    card.changeStatus(CardStatus.ANSWERED);
  }

  @Transactional
  public void delete(AccountAuthDTO account, Long cardId) {
    PrayerCard card = requireMine(account, cardId);
    removeCards(List.of(card));
  }

  // ---------------------------------------------------------------------------
  // 뽑기와 기도하기
  // ---------------------------------------------------------------------------

  /** 기도를 가장 덜 받은 카드 중에서 한 장을 무작위로 뽑는다 */
  @Transactional
  public DrawResponse draw(AccountAuthDTO account) {
    PrayerSetting setting = settingService.getEffective(account.getEcclesiaUid());
    PrayerAccess.requireParticipant(account, setting);
    LocalDateTime now = LocalDateTime.now();

    long used = 0;
    int limit = setting.getDailyDrawLimit();
    if (limit > 0) {
      used = drawRepository.countByDrawerUidAndDrawnAtGreaterThanEqual(account.getUid(),
          LocalDate.now().atStartOfDay());
      if (used >= limit) {
        throw new IllegalArgumentException("오늘 뽑을 수 있는 횟수를 모두 썼어요. 내일 다시 뽑아 주세요.");
      }
    }

    List<PrayerCard> candidates = cardRepository.findDrawCandidates(account.getEcclesiaUid(),
        account.getUid(), now, CardStatus.ACTIVE, CardScope.CHURCH,
        PageRequest.of(0, DRAW_POOL_SIZE));
    if (candidates.isEmpty()) {
      return new DrawResponse(null, limit > 0 ? (int) (limit - used) : null);
    }

    PrayerCard picked = pickLeastPrayed(candidates);
    drawRepository.save(new PrayerCardDraw(picked.getId(), account.getUid(), now));
    Integer remaining = limit > 0 ? (int) (limit - used - 1) : null;
    return new DrawResponse(toCardView(picked, false), remaining);
  }

  /** 후보는 기도 수가 적은 순으로 정렬돼 있다. 가장 적은 수와 같은 것들 중에서 고른다 */
  PrayerCard pickLeastPrayed(List<PrayerCard> sortedCandidates) {
    int least = sortedCandidates.get(0).getResponseCount();
    List<PrayerCard> pool = sortedCandidates.stream()
        .filter(c -> c.getResponseCount() == least).toList();
    return pool.get(random.nextInt(pool.size()));
  }

  @Transactional(readOnly = true)
  public CardView get(AccountAuthDTO account, Long cardId) {
    PrayerSetting setting = settingService.getEffective(account.getEcclesiaUid());
    PrayerAccess.requireParticipant(account, setting);
    PrayerCard card = requireVisible(account, cardId, LocalDateTime.now());
    return toCardView(card,
        responseRepository.existsByCardIdAndResponderUid(card.getId(), account.getUid()));
  }

  @Transactional
  public RespondResponse respond(AccountAuthDTO account, Long cardId, RespondRequest request) {
    PrayerSetting setting = settingService.getEffective(account.getEcclesiaUid());
    PrayerAccess.requireParticipant(account, setting);
    PrayerCard card = requireVisible(account, cardId, LocalDateTime.now());
    if (card.getAuthorUid().equals(account.getUid())) {
      throw new IllegalArgumentException("내 기도제목에는 기도했어요를 남길 수 없습니다.");
    }
    if (responseRepository.existsByCardIdAndResponderUid(card.getId(), account.getUid())) {
      return new RespondResponse(card.getId(), card.getResponseCount(), true);
    }

    String cheerCode = resolveCheer(setting, request == null ? null : request.cheerCode());
    boolean showName = switch (card.getResponderVisible()) {
      case NAMED -> true;
      case RESPONDER_CHOICE -> request != null && Boolean.TRUE.equals(request.showName());
      case HIDDEN -> false;
    };

    try {
      responseRepository.saveAndFlush(new PrayerCardResponse(card.getId(), account.getUid(),
          showName, cheerCode, LocalDateTime.now()));
    } catch (DataIntegrityViolationException e) {
      // 같은 사람이 동시에 두 번 눌러도 한 번만 센다
      return new RespondResponse(card.getId(), card.getResponseCount(), true);
    }
    cardRepository.increaseResponseCount(card.getId());
    return new RespondResponse(card.getId(), card.getResponseCount() + 1, false);
  }

  // ---------------------------------------------------------------------------
  // 신고
  // ---------------------------------------------------------------------------

  /** 신고를 접수한다. 같은 사람의 중복 신고는 무시하고, 설정한 수에 닿으면 카드를 숨긴다 */
  @Transactional
  public void report(AccountAuthDTO account, Long cardId, ReportRequest request) {
    PrayerSetting setting = settingService.getEffective(account.getEcclesiaUid());
    PrayerAccess.requireParticipant(account, setting);
    PrayerCard card = requireVisible(account, cardId, LocalDateTime.now());
    if (card.getAuthorUid().equals(account.getUid())) {
      throw new IllegalArgumentException("내 기도제목은 신고할 수 없습니다.");
    }
    if (reportRepository.existsByCardIdAndReporterUid(card.getId(), account.getUid())) {
      return;
    }
    String detail = request.detail() == null || request.detail().isBlank()
        ? null : request.detail().trim();
    reportRepository.save(new PrayerCardReport(card.getId(), account.getUid(),
        request.reason().trim(), detail, LocalDateTime.now()));

    card.increaseReportCount();
    if (card.getReportCount() >= setting.getAutoHideThreshold()
        && card.getStatus() == CardStatus.ACTIVE) {
      card.changeStatus(CardStatus.HIDDEN);
      log.info("[PRAYER] 신고 누적으로 카드 숨김 cardId:{}, reports:{}", card.getId(),
          card.getReportCount());
    }
  }

  // ---------------------------------------------------------------------------
  // 트리
  // ---------------------------------------------------------------------------

  @Transactional(readOnly = true)
  public TreeResponse tree(AccountAuthDTO account) {
    PrayerSetting setting = settingService.getEffective(account.getEcclesiaUid());
    PrayerAccess.requireParticipant(account, setting);
    LocalDateTime now = LocalDateTime.now();
    PrayerTreeEvent event = settingService.getTreeEvent(account.getEcclesiaUid());
    if (!event.isRunning(now)) {
      throw new IllegalArgumentException("지금 진행 중인 트리 이벤트가 없어요.");
    }

    Long ecclesiaUid = account.getEcclesiaUid();
    List<PrayerCard> cards = cardRepository.findTreeCards(ecclesiaUid, now, CardStatus.ACTIVE,
        CardScope.CHURCH, PageRequest.of(0, TREE_SLOTS));
    List<TreeCard> treeCards = new ArrayList<>();
    for (int i = 0; i < cards.size(); i++) {
      treeCards.add(new TreeCard(cards.get(i).getId(), i, cards.get(i).getResponseCount() > 0));
    }
    long total = cardRepository.countTreeCards(ecclesiaUid, now, CardStatus.ACTIVE,
        CardScope.CHURCH);
    long lit = cardRepository.countLitTreeCards(ecclesiaUid, now, CardStatus.ACTIVE,
        CardScope.CHURCH);
    return new TreeResponse(treeCards, lit, total, progress(lit, total, event.getMinCards()),
        event.getMinCards());
  }

  /** 점등률(%). 카드가 최소 기준보다 적으면 보여 주지 않는다 (적은 교회가 쉽게 100% 가 되는 것을 막는다) */
  static Integer progress(long lit, long total, int minCards) {
    if (total < minCards || total == 0) {
      return null;
    }
    return (int) (lit * 100 / total);
  }

  // ---------------------------------------------------------------------------
  // 탈퇴 정리
  // ---------------------------------------------------------------------------

  /** 탈퇴한 계정의 기도 쪽지 데이터를 모두 지운다 (내 카드, 내가 남긴 기도·신고·뽑기 기록) */
  @Transactional
  public void removeAllByAccount(long accountUid) {
    removeCards(cardRepository.findByAuthorUid(accountUid));
    responseRepository.deleteByResponderUid(accountUid);
    reportRepository.deleteByReporterUid(accountUid);
    drawRepository.deleteByDrawerUid(accountUid);
  }

  private void removeCards(List<PrayerCard> cards) {
    if (cards.isEmpty()) {
      return;
    }
    List<Long> ids = cards.stream().map(PrayerCard::getId).toList();
    responseRepository.deleteByCardIdIn(ids);
    drawRepository.deleteByCardIdIn(ids);
    reportRepository.deleteByCardIdIn(ids);
    cardRepository.deleteAll(cards);
  }

  // ---------------------------------------------------------------------------
  // 내부 도우미
  // ---------------------------------------------------------------------------

  private PrayerCard requireMine(AccountAuthDTO account, Long cardId) {
    PrayerAccess.requireParticipant(account,
        settingService.getEffective(account.getEcclesiaUid()));
    PrayerCard card = cardRepository.findByIdAndEcclesiaUid(cardId, account.getEcclesiaUid())
        .orElseThrow(() -> new NoSuchElementException("기도제목을 찾을 수 없습니다."));
    if (!card.getAuthorUid().equals(account.getUid())) {
      throw new NoSuchElementException("기도제목을 찾을 수 없습니다.");
    }
    return card;
  }

  /** 이 사람에게 보여도 되는 카드. 다른 교회·숨김·만료·차단·내가 속하지 않은 그룹 카드는 없는 것처럼 취급한다 */
  private PrayerCard requireVisible(AccountAuthDTO account, Long cardId, LocalDateTime now) {
    PrayerCard card = cardRepository.findByIdAndEcclesiaUid(cardId, account.getEcclesiaUid())
        .orElseThrow(() -> new NoSuchElementException("기도제목을 찾을 수 없습니다."));
    if (card.getAuthorUid().equals(account.getUid())) {
      return card;
    }
    boolean visible = card.isOpen(now)
        && !blockRepository.existsByBlockerUidAndBlockedUid(account.getUid(),
        card.getAuthorUid())
        && (card.getScope() == CardScope.CHURCH
        || pushGroupMemberRepository.existsByGroupIdAndAccountUid(card.getGroupId(),
        account.getUid()));
    if (!visible) {
      throw new NoSuchElementException("기도제목을 찾을 수 없습니다.");
    }
    return card;
  }

  static DisplayMode resolveDisplayMode(AuthorMode mode, DisplayMode requested) {
    return switch (mode) {
      case ANON_ONLY -> {
        if (requested == DisplayMode.NAME) {
          throw new IllegalArgumentException("우리 교회에서는 익명으로만 올릴 수 있습니다.");
        }
        yield DisplayMode.ANON;
      }
      case NAME_ONLY -> {
        if (requested == DisplayMode.ANON) {
          throw new IllegalArgumentException("우리 교회에서는 이름을 공개해서만 올릴 수 있습니다.");
        }
        yield DisplayMode.NAME;
      }
      case AUTHOR_CHOICE -> {
        if (requested == null) {
          throw new IllegalArgumentException("익명으로 올릴지 이름을 공개할지 선택해 주세요.");
        }
        yield requested;
      }
    };
  }

  /** 허용된 노출 기간만 받는다. 0 이면 상시(null) */
  static LocalDateTime resolveExpiry(PrayerSetting setting, Integer requestedDays,
      LocalDateTime now) {
    List<Integer> allowed = setting.expiryDays();
    int days = requestedDays == null ? allowed.get(0) : requestedDays;
    if (!allowed.contains(days)) {
      throw new IllegalArgumentException("선택할 수 없는 노출 기간입니다.");
    }
    return days == 0 ? null : now.plusDays(days);
  }

  private Long validateGroup(AccountAuthDTO account, PrayerSetting setting, Long groupId) {
    if (setting.getScopeMode() != ScopeMode.GROUP_ALLOWED) {
      throw new IllegalArgumentException("우리 교회에서는 그룹에게만 공개할 수 없습니다.");
    }
    if (groupId == null) {
      throw new IllegalArgumentException("공개할 그룹을 선택해 주세요.");
    }
    pushGroupRepository.findByIdAndEcclesiaUid(groupId, account.getEcclesiaUid())
        .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
    if (!pushGroupMemberRepository.existsByGroupIdAndAccountUid(groupId, account.getUid())) {
      throw new IllegalArgumentException("내가 속한 그룹에만 올릴 수 있습니다.");
    }
    return groupId;
  }

  private static String resolveCheer(PrayerSetting setting, String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    if (!setting.isCheerEnabled()) {
      throw new IllegalArgumentException("우리 교회에서는 응원 문구를 쓰지 않습니다.");
    }
    try {
      return Cheer.valueOf(code.trim()).name();
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("알 수 없는 응원 문구입니다.");
    }
  }

  private CardView toCardView(PrayerCard card, boolean responded) {
    String authorName = null;
    if (card.getDisplayMode() == DisplayMode.NAME) {
      authorName = accountRepository.findById(card.getAuthorUid())
          .map(PrayerCardService::displayName).orElse(null);
    }
    return new CardView(card.getId(), card.getContent(), card.getDisplayMode(), authorName,
        card.getScope(), card.getExpiresAt(), card.getInsertTime(), responded,
        responderNotice(card.getResponderVisible()));
  }

  /** 기도하기 직전에 보여 줄 공개 정책 안내 문구. 카드를 올린 시점의 정책을 따른다 */
  static String responderNotice(ResponderVisibility visibility) {
    return switch (visibility) {
      case HIDDEN -> "기도해 주신 분의 이름은 작성자에게 보이지 않아요.";
      case NAMED -> "기도하면 내 이름이 작성자에게 보여요.";
      case RESPONDER_CHOICE -> "원하면 내 이름을 작성자에게 알릴 수 있어요.";
    };
  }

  private MyCard toMyCard(PrayerCard card, List<PrayerCardResponse> responses,
      LocalDateTime now) {
    List<Long> namedUids = responses.stream()
        .filter(r -> card.getResponderVisible() == ResponderVisibility.NAMED
            || (card.getResponderVisible() == ResponderVisibility.RESPONDER_CHOICE
            && r.isShowName()))
        .map(PrayerCardResponse::getResponderUid).toList();
    Map<Long, Account> accounts = namedUids.isEmpty() ? Map.of()
        : accountRepository.findAllById(namedUids).stream()
            .collect(Collectors.toMap(Account::getUid, Function.identity(), (a, b) -> a));
    List<String> responders = namedUids.stream()
        .map(accounts::get).filter(java.util.Objects::nonNull)
        .map(PrayerCardService::displayName).toList();

    Map<Cheer, Integer> counted = new EnumMap<>(Cheer.class);
    for (PrayerCardResponse r : responses) {
      if (r.getCheerCode() != null) {
        counted.merge(Cheer.valueOf(r.getCheerCode()), 1, Integer::sum);
      }
    }
    Map<String, Integer> cheers = new LinkedHashMap<>();
    counted.forEach((cheer, count) -> cheers.put(cheer.name(), count));

    return new MyCard(card.getId(), card.getContent(), card.getDisplayMode(), card.getScope(),
        card.getStatus(), card.getExpiresAt(), card.getInsertTime(), card.getResponseCount(),
        responders, cheers);
  }

  static String displayName(Account account) {
    String nickname = account.getNickname();
    if (nickname != null && !nickname.isBlank()) {
      return nickname;
    }
    return account.getName() == null ? "" : account.getName();
  }
}
