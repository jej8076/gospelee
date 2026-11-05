package com.gospelee.api.service.prayer;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.enums.EcclesiaStatusType;
import com.gospelee.api.enums.PrayerEnums.WhoCanWrite;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.service.announcement.AnnouncementAccessPolicy;
import org.springframework.security.access.AccessDeniedException;

/** 기도 쪽지 접근 규칙. 컨트롤러가 넘긴 로그인 계정과 교회 설정만으로 판단한다 */
final class PrayerAccess {

  private PrayerAccess() {
  }

  /** 탈퇴하지 않았고 소속 교회가 있는 계정 */
  static boolean isMember(AccountAuthDTO account) {
    return account != null && account.getEcclesiaUid() != null && account.getLeaveYn() != Yn.Y;
  }

  /**
   * 기도 쪽지를 쓰고 읽고 기도할 수 있는 계정.
   * 기본(APPROVED_ONLY)은 교회 가입이 승인된 성도만, ALL_MEMBERS 이면 가입 요청 중인 성도도 포함한다.
   */
  static boolean canParticipate(AccountAuthDTO account, PrayerSetting setting) {
    if (!isMember(account)) {
      return false;
    }
    return setting.getWhoCanWrite() == WhoCanWrite.ALL_MEMBERS
        || EcclesiaStatusType.APPROVAL.getName().equals(account.getEcclesiaStatus());
  }

  /** 기능이 켜져 있고 참여할 수 있는지 확인한다. 서버가 최종 판단하므로 모든 성도용 API 가 먼저 부른다 */
  static void requireParticipant(AccountAuthDTO account, PrayerSetting setting) {
    if (!isMember(account)) {
      throw new AccessDeniedException("소속된 교회가 있어야 이용할 수 있습니다.");
    }
    if (!setting.isEnabled()) {
      throw new AccessDeniedException("우리 교회에서는 아직 기도 쪽지를 열지 않았습니다.");
    }
    if (!canParticipate(account, setting)) {
      throw new AccessDeniedException("교회 가입이 승인된 성도만 이용할 수 있습니다.");
    }
  }

  static void requireManager(AccountAuthDTO account) {
    if (!AnnouncementAccessPolicy.canWriteEcclesia(account)) {
      throw new AccessDeniedException("기도 쪽지를 관리할 권한이 없습니다.");
    }
  }
}
