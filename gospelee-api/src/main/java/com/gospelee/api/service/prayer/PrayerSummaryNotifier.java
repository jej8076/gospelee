package com.gospelee.api.service.prayer;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PrayerCard;
import com.gospelee.api.entity.PrayerCardResponse;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.enums.DeepLinkRouterPath;
import com.gospelee.api.enums.PushNotificationDataType;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardResponseRepository;
import com.gospelee.api.service.FirebaseService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * 하루 한 번, 어제 기도를 받은 작성자에게 요약 푸시를 보낸다 ("어제 N명이 기도해 주셨어요").
 * 기도마다 알리지 않아 알림이 쌓이지 않고, 누가 기도했는지는 담지 않는다.
 * 교회 설정에서 기능이 꺼져 있거나 요약 푸시를 끈 교회는 보내지 않는다. 전송 실패는 로그만 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrayerSummaryNotifier {

  private final PrayerCardResponseRepository responseRepository;
  private final PrayerCardRepository cardRepository;
  private final AccountRepository accountRepository;
  private final PrayerSettingService settingService;
  private final FirebaseService firebaseService;

  @Scheduled(cron = "0 0 9 * * *")
  public void sendDailySummary() {
    try {
      sendSummary(LocalDate.now().atStartOfDay());
    } catch (Exception e) {
      log.warn("[PRAYER_PUSH] 일일 요약 푸시 실패", e);
    }
  }

  /** [todayStart - 1일, todayStart) 사이에 받은 기도를 작성자별로 모아 보낸다 */
  void sendSummary(LocalDateTime todayStart) {
    List<PrayerCardResponse> responses = responseRepository
        .findByInsertTimeGreaterThanEqualAndInsertTimeLessThan(todayStart.minusDays(1),
            todayStart);
    if (responses.isEmpty()) {
      return;
    }
    Map<Long, PrayerCard> cards = cardRepository.findAllById(
            responses.stream().map(PrayerCardResponse::getCardId).distinct().toList()).stream()
        .collect(Collectors.toMap(PrayerCard::getId, Function.identity()));

    Map<Long, Integer> countByAuthor = new HashMap<>();
    Map<Long, Long> ecclesiaByAuthor = new HashMap<>();
    for (PrayerCardResponse r : responses) {
      PrayerCard card = cards.get(r.getCardId());
      if (card != null) {
        countByAuthor.merge(card.getAuthorUid(), 1, Integer::sum);
        ecclesiaByAuthor.put(card.getAuthorUid(), card.getEcclesiaUid());
      }
    }

    Map<Long, PrayerSetting> settings = new HashMap<>();
    for (Map.Entry<Long, Integer> entry : countByAuthor.entrySet()) {
      PrayerSetting setting = settings.computeIfAbsent(ecclesiaByAuthor.get(entry.getKey()),
          settingService::getEffective);
      if (setting.isEnabled() && setting.isSummaryPush()) {
        send(entry.getKey(), entry.getValue());
      }
    }
  }

  private void send(Long authorUid, int count) {
    try {
      Account author = accountRepository.findById(authorUid).orElse(null);
      if (author == null || ObjectUtils.isEmpty(author.getPushToken())) {
        return;
      }
      firebaseService.sendNotification(author.getPushToken(), "기도 쪽지",
          "어제 " + count + "번 기도해 주셨어요.",
          Map.of(PushNotificationDataType.ROUTE.lower(), DeepLinkRouterPath.CHURCH.path()));
    } catch (Exception e) {
      log.warn("[PRAYER_PUSH] 전송 실패 accountUid={}", authorUid, e);
    }
  }
}
