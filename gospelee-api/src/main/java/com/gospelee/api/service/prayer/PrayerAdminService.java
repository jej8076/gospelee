package com.gospelee.api.service.prayer;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.prayer.PrayerDTOs.AdminCardItem;
import com.gospelee.api.dto.prayer.PrayerDTOs.AdminStats;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PrayerCard;
import com.gospelee.api.entity.PrayerCardReport;
import com.gospelee.api.enums.PrayerEnums.AdminSeesAnon;
import com.gospelee.api.enums.PrayerEnums.CardStatus;
import com.gospelee.api.enums.PrayerEnums.DisplayMode;
import com.gospelee.api.enums.PrayerEnums.ReportStatus;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardReportRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 교회 관리자의 기도 쪽지 관리: 신고 처리, 사전 검수, 통계.
 * 익명 카드의 작성자는 카드를 올린 시점에 박제된 정책(admin_sees_anon)이 허용할 때만 내려 준다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrayerAdminService {

  private final PrayerCardRepository cardRepository;
  private final PrayerCardReportRepository reportRepository;
  private final AccountRepository accountRepository;

  /** 처리 대기 중인 신고가 있는 카드 */
  @Transactional(readOnly = true)
  public List<AdminCardItem> reportedCards(AccountAuthDTO account) {
    PrayerAccess.requireManager(account);
    List<PrayerCard> cards = cardRepository.findWithPendingReports(account.getEcclesiaUid(),
        ReportStatus.PENDING);
    Map<Long, List<String>> reasons = reportRepository
        .findByCardIdIn(cards.stream().map(PrayerCard::getId).toList()).stream()
        .filter(r -> r.getStatus() == ReportStatus.PENDING)
        .collect(Collectors.groupingBy(PrayerCardReport::getCardId,
            Collectors.mapping(PrayerCardReport::getReason, Collectors.toList())));
    return toItems(cards, true, reasons);
  }

  /** 사전 검수를 켠 교회의 승인 대기 카드 */
  @Transactional(readOnly = true)
  public List<AdminCardItem> pendingCards(AccountAuthDTO account) {
    PrayerAccess.requireManager(account);
    return toItems(cardRepository.findByEcclesiaUidAndStatusOrderByIdAsc(
        account.getEcclesiaUid(), CardStatus.PENDING), false, Map.of());
  }

  /** 카드를 내린다. 이 카드의 대기 신고는 "내림"으로 처리된다 */
  @Transactional
  public void hide(AccountAuthDTO account, Long cardId) {
    PrayerCard card = requireCard(account, cardId);
    card.changeStatus(CardStatus.HIDDEN);
    closeReports(card, ReportStatus.RESOLVED);
  }

  /** 숨긴 카드를 다시 보인다. 이 카드의 대기 신고는 "문제 없음"으로 처리된다 */
  @Transactional
  public void restore(AccountAuthDTO account, Long cardId) {
    PrayerCard card = requireCard(account, cardId);
    card.changeStatus(CardStatus.ACTIVE);
    closeReports(card, ReportStatus.DISMISSED);
  }

  @Transactional
  public void approve(AccountAuthDTO account, Long cardId) {
    PrayerCard card = requireCard(account, cardId);
    if (card.getStatus() != CardStatus.PENDING) {
      throw new IllegalArgumentException("승인을 기다리는 카드가 아닙니다.");
    }
    card.changeStatus(CardStatus.ACTIVE);
  }

  @Transactional
  public void reject(AccountAuthDTO account, Long cardId) {
    PrayerCard card = requireCard(account, cardId);
    if (card.getStatus() != CardStatus.PENDING) {
      throw new IllegalArgumentException("승인을 기다리는 카드가 아닙니다.");
    }
    card.changeStatus(CardStatus.HIDDEN);
  }

  /** 개인을 식별할 수 없는 집계만 내려 준다 */
  @Transactional(readOnly = true)
  public AdminStats stats(AccountAuthDTO account) {
    PrayerAccess.requireManager(account);
    Long ecclesiaUid = account.getEcclesiaUid();
    return new AdminStats(
        cardRepository.countByEcclesiaUid(ecclesiaUid),
        cardRepository.countByEcclesiaUidAndStatus(ecclesiaUid, CardStatus.ACTIVE),
        cardRepository.countByEcclesiaUidAndStatus(ecclesiaUid, CardStatus.ANSWERED),
        cardRepository.countByEcclesiaUidAndStatus(ecclesiaUid, CardStatus.HIDDEN),
        cardRepository.countByEcclesiaUidAndStatus(ecclesiaUid, CardStatus.PENDING),
        cardRepository.countOpenWithoutResponse(ecclesiaUid, LocalDateTime.now(),
            CardStatus.ACTIVE),
        cardRepository.sumResponseCount(ecclesiaUid),
        cardRepository.findWithPendingReports(ecclesiaUid, ReportStatus.PENDING).size());
  }

  /**
   * 관리자에게 작성자를 보여도 되는지. 이름을 공개한 카드는 항상 보이고,
   * 익명 카드는 카드에 박제된 정책이 ALWAYS 이거나 (신고 목록에서 볼 때) REPORTED_ONLY 일 때만 보인다.
   */
  static boolean authorVisibleToAdmin(PrayerCard card, boolean viewingReported) {
    if (card.getDisplayMode() == DisplayMode.NAME) {
      return true;
    }
    return card.getAdminSeesAnon() == AdminSeesAnon.ALWAYS
        || (viewingReported && card.getAdminSeesAnon() == AdminSeesAnon.REPORTED_ONLY);
  }

  private List<AdminCardItem> toItems(List<PrayerCard> cards, boolean viewingReported,
      Map<Long, List<String>> reasons) {
    List<Long> visibleAuthors = cards.stream()
        .filter(c -> authorVisibleToAdmin(c, viewingReported))
        .map(PrayerCard::getAuthorUid).distinct().toList();
    Map<Long, String> names = visibleAuthors.isEmpty() ? Map.of()
        : accountRepository.findAllById(visibleAuthors).stream()
            .collect(Collectors.toMap(Account::getUid, PrayerCardService::displayName,
                (a, b) -> a));
    return cards.stream()
        .map(c -> new AdminCardItem(c.getId(), c.getContent(), c.getDisplayMode(), c.getStatus(),
            c.getReportCount(), c.getResponseCount(), c.getInsertTime(),
            authorVisibleToAdmin(c, viewingReported) ? names.get(c.getAuthorUid()) : null,
            reasons.getOrDefault(c.getId(), List.of())))
        .toList();
  }

  private PrayerCard requireCard(AccountAuthDTO account, Long cardId) {
    PrayerAccess.requireManager(account);
    return cardRepository.findByIdAndEcclesiaUid(cardId, account.getEcclesiaUid())
        .orElseThrow(() -> new NoSuchElementException("기도제목을 찾을 수 없습니다."));
  }

  private void closeReports(PrayerCard card, ReportStatus status) {
    reportRepository.findByCardIdAndStatus(card.getId(), ReportStatus.PENDING)
        .forEach(r -> r.close(status));
  }
}
