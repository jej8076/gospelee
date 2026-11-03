package com.gospelee.api.service.push;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.pushhistory.PushReceiptRequestDTO;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.PushNotificationSendStatusType;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 앱이 알려 주는 푸시 수신·확인을 내 수신 기록에 반영한다 */
@Service
@RequiredArgsConstructor
public class PushReceiptService {

  private final PushNotificationReceiversRepository receiversRepository;

  /**
   * 내 수신 기록의 상태를 앞으로만 바꾼다
   *
   * @return 상태를 바꿨으면 true. 내 기록이 아니거나 이미 같은/더 나아간 상태면 false
   */
  @Transactional
  public boolean report(PushReceiptRequestDTO dto) {
    PushNotificationSendStatusType reported = parse(dto.getStatus());
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    return receiversRepository
        .findByPushNotificationIdAndReceiveAccountUid(dto.getPushId(), account.getUid())
        .map(receiver -> advance(receiver, reported))
        .orElse(false);
  }

  private boolean advance(PushNotificationReceivers receiver,
      PushNotificationSendStatusType reported) {
    PushNotificationSendStatusType current = PushNotificationSendStatusType.fromName(
        receiver.getStatus());
    if (reported.progress() <= current.progress()) {
      return false;
    }
    receiver.changeStatus(reported.name());
    return true;
  }

  private static PushNotificationSendStatusType parse(String status) {
    PushNotificationSendStatusType type = PushNotificationSendStatusType.fromName(status);
    if (type != PushNotificationSendStatusType.RECEIVED
        && type != PushNotificationSendStatusType.CONFIRMED) {
      throw new IllegalArgumentException("보고할 수 없는 상태입니다.");
    }
    return type;
  }
}
