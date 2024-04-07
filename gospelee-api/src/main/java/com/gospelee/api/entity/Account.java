package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Entity
@ToString
@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private long uid;

  @Column
  private String name;

  @Column
  private String rrn;

  @Column
  private String phone;

  @OneToOne
  @JoinColumn(name = "accessToken")
  private AccountKakaoToken accountKakaoToken;

  @OneToMany
  @JoinColumn(name = "parentUid")
  private List<AccountKakaoToken> accountKakaoTokenList;

  public AccountKakaoToken toAccountKakaoToken(long parentUid) {
    return AccountKakaoToken.builder()
        .parentUid(parentUid)
        .accessToken(this.getAccountKakaoToken().getAccessToken())
        .accessTokenExpiresAt(this.getAccountKakaoToken().getAccessTokenExpiresAt())
        .refreshToken(this.getAccountKakaoToken().getRefreshToken())
        .refreshTokenExpiresAt(this.getAccountKakaoToken().getRefreshTokenExpiresAt())
        .idToken(this.getAccountKakaoToken().getIdToken())
        .deviceInfo(this.getAccountKakaoToken().getDeviceInfo())
        .build();
  }

}