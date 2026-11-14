package com.gospelee.api.service.ministry;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ministry;
import com.gospelee.api.enums.DeepLinkRouterPath;
import com.gospelee.api.enums.PushNotificationDataType;
import com.gospelee.api.service.FirebaseService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/** 모임 가입 신청·결과 푸시. 전송 실패가 가입 처리에 영향을 주지 않도록 예외는 로그로만 남긴다 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinistryPushNotifier {

  private final FirebaseService firebaseService;

  public void notifyJoinRequested(Account manager, Ministry ministry, String requesterName) {
    String name = ObjectUtils.isEmpty(requesterName) ? "새 멤버" : requesterName;
    send(manager, "모임 가입 신청",
        name + "님이 [" + ministry.getName() + "] 가입을 신청했어요. 눌러서 승인해주세요.");
  }

  public void notifyJoinDecided(Account target, Ministry ministry, boolean approved) {
    send(target, approved ? "모임 가입 승인" : "모임 가입 거절",
        approved ? "[" + ministry.getName() + "] 가입이 승인되었어요."
            : "[" + ministry.getName() + "] 가입 신청이 거절되었어요.");
  }

  public void notifyAnnouncementReported(Account manager, Ministry ministry) {
    send(manager, "모임 공지 신고",
        "[" + ministry.getName() + "] 공지에 신고가 접수되었어요. 운영자가 확인해 처리해요.");
  }

  private void send(Account account, String title, String body) {
    if (account == null || ObjectUtils.isEmpty(account.getPushToken())) {
      return;
    }
    try {
      firebaseService.sendNotification(account.getPushToken(), title, body,
          Map.of(PushNotificationDataType.ROUTE.lower(), DeepLinkRouterPath.CHURCH.path()));
    } catch (Exception e) {
      log.warn("[MINISTRY_PUSH] 전송 실패 accountUid={}", account.getUid(), e);
    }
  }
}
