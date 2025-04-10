package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.RoleType;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

  /**
   * 사용자가 Ecclesia 상태를 업데이트할 수 있는 권한이 있는지 확인
   */
  public boolean canUpdateEcclesiaStatus(AccountAuthDTO user, Ecclesia ecclesia) {

    // 관리자는 항상 수정 가능
    if (!RoleType.ADMIN.equals(user.getRole())) {
      return true;
    }

    // 마스터 계정만 수정 가능
    return user.getUid() == ecclesia.getMasterAccountUid();
  }
}