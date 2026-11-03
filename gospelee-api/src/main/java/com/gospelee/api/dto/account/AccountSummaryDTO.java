package com.gospelee.api.dto.account;

import com.gospelee.api.entity.Account;
import com.gospelee.api.enums.RoleType;
import lombok.Builder;
import lombok.Getter;

/**
 * 계정 목록 응답용. 주민번호·idToken·pushToken 같은 민감 정보는 담지 않는다
 */
@Getter
@Builder
public class AccountSummaryDTO {

  private long uid;
  private String name;
  private String nickname;
  private String email;
  private String phone;
  private RoleType role;
  private Long ecclesiaUid;

  public static AccountSummaryDTO from(Account account) {
    return AccountSummaryDTO.builder()
        .uid(account.getUid())
        .name(account.getName())
        .nickname(account.getNickname())
        .email(account.getEmail())
        .phone(account.getPhone())
        .role(account.getRole())
        .ecclesiaUid(account.getEcclesiaUid())
        .build();
  }
}
