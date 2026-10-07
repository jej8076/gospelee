package com.gospelee.api.service.announcement;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.service.EcclesiaPushNotifier;

/**
 * 공지사항 작성/수정 권한 정책
 * 교회 공지는 같은 교회의 담임목사/교역자/관리자만 작성하고 수정할 수 있다.
 */
final class AnnouncementAccessPolicy {

  private AnnouncementAccessPolicy() {
  }

  /**
   * 교회 공지 작성 권한: 소속 교회가 있고 관리 역할인 경우
   */
  static boolean canWriteEcclesia(AccountAuthDTO account) {
    return account != null
        && account.getEcclesiaUid() != null
        && EcclesiaPushNotifier.isManagerRole(account.getRole());
  }

  /**
   * 교회 공지 수정 권한: 작성 권한이 있고, 수정 대상이 내 교회의 공지인 경우
   */
  static boolean canModifyEcclesia(AccountAuthDTO account, Announcement existing) {
    return canWriteEcclesia(account)
        && OrganizationType.ECCLESIA.name().equals(existing.getOrganizationType())
        && account.getEcclesiaUid().equals(existing.getOrganizationId());
  }
}
