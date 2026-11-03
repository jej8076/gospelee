package com.gospelee.api.service.push;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.pushhistory.PushHistoryDTO;
import com.gospelee.api.dto.pushhistory.PushHistoryDetailDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PushNotification;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.PushNotificationSendStatusType;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationRepository;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.announcement.AnnouncementAccessPolicy;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 우리 교회에서 보낸 푸시의 전달·수신·확인 현황. 같은 교회의 교역자/관리자 전용 */
@Service
@RequiredArgsConstructor
public class PushHistoryService {

  private static final int LIST_SIZE = 30;

  private final PushNotificationRepository pushNotificationRepository;
  private final PushNotificationReceiversRepository receiversRepository;
  private final AccountService accountService;

  @Transactional(readOnly = true)
  public List<PushHistoryDTO> getList() {
    AccountAuthDTO me = requireManager();
    List<PushNotification> pushes = pushNotificationRepository.findRecentByEcclesia(
        me.getEcclesiaUid(), PageRequest.of(0, LIST_SIZE));
    if (pushes.isEmpty()) {
      return List.of();
    }
    Map<Long, Account> accounts = accountsOfEcclesia(me.getEcclesiaUid());
    Map<Long, Map<String, Integer>> counts = countsByPush(
        pushes.stream().map(PushNotification::getId).toList());
    return pushes.stream()
        .map(p -> toDto(p, accounts, counts.getOrDefault(p.getId(), Map.of())))
        .toList();
  }

  @Transactional(readOnly = true)
  public PushHistoryDetailDTO getDetail(Long pushId) {
    AccountAuthDTO me = requireManager();
    PushNotification push = pushNotificationRepository.findById(pushId)
        .orElseThrow(() -> new IllegalArgumentException("푸시 알림을 찾을 수 없습니다."));
    Map<Long, Account> accounts = accountsOfEcclesia(me.getEcclesiaUid());
    // 다른 교회가 보낸 푸시는 없는 것처럼 취급한다
    if (!accounts.containsKey(push.getSendAccountUid())) {
      throw new IllegalArgumentException("푸시 알림을 찾을 수 없습니다.");
    }
    List<PushNotificationReceivers> receivers = receiversRepository.findByPushNotificationId(
        pushId);
    Map<String, Integer> counts = new HashMap<>();
    receivers.forEach(r -> counts.merge(r.getStatus(), 1, Integer::sum));

    return PushHistoryDetailDTO.builder()
        .push(toDto(push, accounts, counts))
        .receivers(receivers.stream().map(r -> PushHistoryDetailDTO.Receiver.builder()
            .accountUid(r.getReceiveAccountUid())
            .name(Optional.ofNullable(accounts.get(r.getReceiveAccountUid()))
                .map(Account::getName).orElse("(알 수 없음)"))
            .status(r.getStatus())
            .build()).toList())
        .build();
  }

  private PushHistoryDTO toDto(PushNotification p, Map<Long, Account> accounts,
      Map<String, Integer> counts) {
    int ready = counts.getOrDefault(PushNotificationSendStatusType.READY.name(), 0)
        + counts.getOrDefault(PushNotificationSendStatusType.SENT.name(), 0);
    int received = counts.getOrDefault(PushNotificationSendStatusType.RECEIVED.name(), 0);
    int confirmed = counts.getOrDefault(PushNotificationSendStatusType.CONFIRMED.name(), 0);
    int failed = counts.getOrDefault(PushNotificationSendStatusType.FAILED.name(), 0);
    return PushHistoryDTO.builder()
        .pushId(p.getId())
        .title(p.getTitle())
        .message(p.getMessage())
        .category(p.getCategory())
        .senderName(Optional.ofNullable(accounts.get(p.getSendAccountUid()))
            .map(Account::getName).orElse(null))
        .sentTime(p.getInsertTime())
        .targetCount(ready + received + confirmed + failed)
        .deliveredCount(ready + received + confirmed)
        .failedCount(failed)
        .receivedCount(received + confirmed)
        .confirmedCount(confirmed)
        .build();
  }

  private Map<Long, Map<String, Integer>> countsByPush(List<Long> pushIds) {
    Map<Long, Map<String, Integer>> result = new HashMap<>();
    for (Object[] row : receiversRepository.countByStatus(pushIds)) {
      result.computeIfAbsent((Long) row[0], k -> new HashMap<>())
          .put((String) row[1], ((Number) row[2]).intValue());
    }
    return result;
  }

  private Map<Long, Account> accountsOfEcclesia(Long ecclesiaUid) {
    return accountService.getAccountByEcclesiaUid(ecclesiaUid).orElse(List.of()).stream()
        .collect(Collectors.toMap(Account::getUid, Function.identity(), (a, b) -> a));
  }

  private AccountAuthDTO requireManager() {
    AccountAuthDTO me = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!AnnouncementAccessPolicy.canWriteEcclesia(me)) {
      throw new AccessDeniedException("푸시 발송 현황을 볼 권한이 없습니다.");
    }
    return me;
  }
}
