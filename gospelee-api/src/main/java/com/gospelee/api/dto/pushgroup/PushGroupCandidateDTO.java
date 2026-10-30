package com.gospelee.api.dto.pushgroup;

import com.gospelee.api.enums.RoleType;
import lombok.Getter;

/** 그룹 멤버로 고를 수 있는 성도. 토큰·주민번호 등은 담지 않는다 */
@Getter
public class PushGroupCandidateDTO {

  private final Long uid;
  private final String name;
  private final String nickname;
  private final RoleType role;
  /** 푸시 토큰이 있어 알림을 받을 수 있는 계정인지 */
  private final boolean pushEnabled;

  public PushGroupCandidateDTO(Long uid, String name, String nickname, RoleType role,
      boolean pushEnabled) {
    this.uid = uid;
    this.name = name;
    this.nickname = nickname;
    this.role = role;
    this.pushEnabled = pushEnabled;
  }
}
