package com.gospelee.api.service.push;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PushGroup;
import com.gospelee.api.entity.PushGroupMember;
import com.gospelee.api.entity.PushNotification;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.PushNotificationSendStatusType;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupMemberRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationRepository;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.FirebaseService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 교회 푸시 알림 발송. 공지·주보 등록과, 나중의 "등록 없이 직접 보내기"가 함께 쓴다.
 * 받는 사람은 교회 성도 전체이거나, 선택한 그룹의 멤버(여러 그룹이면 합집합, 중복은 한 번)다.
 * 호출하는 쪽의 트랜잭션 안에서 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushDispatchService {

  private final AccountService accountService;
  private final PushGroupRepository pushGroupRepository;
  private final PushGroupMemberRepository pushGroupMemberRepository;
  private final PushNotificationRepository pushNotificationRepository;
  private final PushNotificationReceiversRepository pushNotificationReceiversRepository;
  private final FirebaseService firebaseService;

  @Getter
  @Builder
  public static class PushRequest {

    private final Long sendAccountUid;
    private final Long ecclesiaUid;
    /** 비어 있으면 교회 성도 전체 */
    private final List<Long> groupIds;
    private final String organization;
    private final String category;
    private final String title;
    private final String message;
    private final Map<String, String> data;
  }

  /**
   * 그룹 id 가 모두 이 교회의 그룹인지 확인하고, 중복을 뺀 id 목록을 돌려준다.
   * 저장 작업 전에 불러 두면 잘못된 요청이 데이터를 남기지 않는다.
   *
   * @throws IllegalArgumentException 다른 교회의 그룹이거나 없는 그룹이 섞여 있을 때
   */
  public List<Long> validateGroupIds(Long ecclesiaUid, List<Long> groupIds) {
    if (groupIds == null || groupIds.isEmpty()) {
      return List.of();
    }
    Set<Long> unique = new LinkedHashSet<>(groupIds);
    List<PushGroup> found = pushGroupRepository.findByIdInAndEcclesiaUid(unique, ecclesiaUid);
    if (found.size() != unique.size()) {
      throw new IllegalArgumentException("알림을 보낼 수 없는 그룹이 포함되어 있습니다.");
    }
    return new ArrayList<>(unique);
  }

  /**
   * 알림을 만들어 받는 사람에게 보내고 기록한다
   *
   * @return 저장한 푸시 알림 id. 받을 대상이 없으면 null
   */
  public Long send(PushRequest request) {
    List<Long> groupIds = validateGroupIds(request.getEcclesiaUid(), request.getGroupIds());
    List<Account> recipients = resolveRecipients(request.getEcclesiaUid(), groupIds);
    if (recipients.isEmpty()) {
      return null;
    }

    PushNotification pushNotification = pushNotificationRepository.save(
        PushNotification.builder()
            .sendAccountUid(request.getSendAccountUid())
            .organization(request.getOrganization())
            .category(request.getCategory())
            .title(request.getTitle())
            .message(request.getMessage())
            .build());

    List<PushNotificationReceivers> receivers = new ArrayList<>();
    for (Account account : recipients) {
      // 푸시 토큰이 없으면 보낼 수 없다
      if (account.getPushToken() == null || account.getPushToken().isEmpty()) {
        continue;
      }
      // 한 명의 토큰이 만료/오류여도 나머지에게는 계속 보내고, 실패는 FAILED 로 기록한다
      boolean sent = firebaseService.trySendNotification(account.getPushToken(),
          pushNotification.getTitle(), pushNotification.getMessage(), request.getData());
      receivers.add(PushNotificationReceivers.builder()
          .pushNotificationId(pushNotification.getId())
          .receiveAccountUid(account.getUid())
          .status((sent ? PushNotificationSendStatusType.READY
              : PushNotificationSendStatusType.FAILED).name())
          .build());
    }

    List<PushNotificationReceivers> saved = pushNotificationReceiversRepository.saveAll(receivers);
    pushNotificationRepository.updateTotalcountById(pushNotification.getId(), saved.size());
    log.info("푸시 발송 - pushId:{}, ecclesiaUid:{}, groupIds:{}, 대상:{}명",
        pushNotification.getId(), request.getEcclesiaUid(), groupIds, saved.size());
    return pushNotification.getId();
  }

  /** 교회 성도 중 받을 사람. 그룹을 지정하면 그룹 멤버와 교회 성도의 교집합 */
  List<Account> resolveRecipients(Long ecclesiaUid, List<Long> groupIds) {
    List<Account> churchAccounts = accountService.getAccountByEcclesiaUid(ecclesiaUid)
        .orElse(List.of());
    if (groupIds.isEmpty()) {
      return churchAccounts;
    }
    Set<Long> memberUids = new LinkedHashSet<>();
    for (PushGroupMember member : pushGroupMemberRepository.findByGroupIdIn(groupIds)) {
      memberUids.add(member.getAccountUid());
    }
    return churchAccounts.stream().filter(a -> memberUids.contains(a.getUid())).toList();
  }
}
