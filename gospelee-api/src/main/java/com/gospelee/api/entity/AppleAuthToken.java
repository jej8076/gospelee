package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 애플 로그인 사용자의 refresh token 보관. id token(약 10분)이 만료되면 이 값으로 새 id token 을 받는다.
 * 비밀 값이므로 Account 와 분리하고 toString 에 노출하지 않는다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleAuthToken extends EditInfomation {

  @Id
  @Column(name = "account_uid")
  private Long accountUid;

  @Column(name = "refresh_token", length = 1000, nullable = false)
  private String refreshToken;

  public AppleAuthToken(Long accountUid, String refreshToken) {
    this.accountUid = accountUid;
    this.refreshToken = refreshToken;
  }

  public void changeRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
