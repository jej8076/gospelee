package com.gospelee.api.service;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.DeepLinkRouterPath;
import com.gospelee.api.enums.PushNotificationDataType;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * 교회 가입 요청/결정 푸시 알림 전송
 * 푸시 전송 실패가 가입 처리 자체에 영향을 주지 않도록 모든 예외를 로그로만 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcclesiaPushNotifier {

  // 가입 요청을 처리(승인/반려)할 수 있는 역할
  private static final List<RoleType> MANAGER_ROLES = List.of(
      RoleType.SENIOR_PASTOR, RoleType.PASTOR, RoleType.ADMIN);

  private final FirebaseService firebaseService;
  private final AccountRepository accountRepository;

  public static boolean isManagerRole(RoleType role) {
    return role != null && MANAGER_ROLES.contains(role);
  }

  /**
   * 교회 관리자(교회 대표 및 교역자)에게 새 가입 요청 알림
   */
  public void notifyJoinRequested(Ecclesia ecclesia, String requesterName) {
    try {
      Map<Long, Account> recipients = new LinkedHashMap<>();
      accountRepository.findByEcclesiaUid(ecclesia.getUid()).ifPresent(accounts ->
          accounts.stream()
              .filter(a -> isManagerRole(a.getRole()))
              .forEach(a -> recipients.put(a.getUid(), a)));
      if (ecclesia.getMasterAccountUid() != null) {
        accountRepository.findById(ecclesia.getMasterAccountUid())
            .ifPresent(a -> recipients.putIfAbsent(a.getUid(), a));
      }

      String name = ObjectUtils.isEmpty(requesterName) ? "새 성도" : requesterName;
      String body = name + "님이 " + ecclesia.getName() + " 가입을 요청했어요. 눌러서 승인해주세요.";
      for (Account recipient : recipients.values()) {
        send(recipient, "교회 가입 요청", body, DeepLinkRouterPath.ECCLESIA_JOIN_REQUESTS.path());
      }
    } catch (Exception e) {
      log.warn("[ECCLESIA_PUSH] 가입 요청 알림 전송 실패 ecclesiaUid={}", ecclesia.getUid(), e);
    }
  }

  /**
   * 가입을 요청한 성도에게 승인/반려 결과 알림
   */
  public void notifyJoinDecided(Account target, String ecclesiaName, boolean approved) {
    try {
      String title = approved ? "교회 가입 승인" : "교회 가입 반려";
      String body = approved
          ? ecclesiaName + " 가입이 승인되었어요. 이제 교회 소식을 볼 수 있어요."
          : ecclesiaName + " 가입 요청이 반려되었어요.";
      send(target, title, body, DeepLinkRouterPath.CHURCH.path());
    } catch (Exception e) {
      log.warn("[ECCLESIA_PUSH] 가입 결과 알림 전송 실패 accountUid={}", target.getUid(), e);
    }
  }

  private void send(Account account, String title, String body, String route) {
    if (ObjectUtils.isEmpty(account.getPushToken())) {
      return;
    }
    try {
      firebaseService.sendNotification(account.getPushToken(), title, body,
          Map.of(PushNotificationDataType.ROUTE.lower(), route));
    } catch (Exception e) {
      log.warn("[ECCLESIA_PUSH] 전송 실패 accountUid={}", account.getUid(), e);
    }
  }
}
