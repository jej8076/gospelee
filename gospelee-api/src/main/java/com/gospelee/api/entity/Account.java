package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column
    private String token;

    @OneToOne
    @JoinColumn(name = "accessToken")
    private AccountKakaoToken accountKakaoToken;

    @OneToMany
    @JoinColumn(name = "parentUid")
    private List<AccountKakaoToken> accountKakaoTokenList;

    public AccountKakaoToken toAccountKakaoToken(long parentUid){
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