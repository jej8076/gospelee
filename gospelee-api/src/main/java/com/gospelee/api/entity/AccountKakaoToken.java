package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountKakaoToken extends EditInfomation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long uid;

    @Column
    private long parentUid;

//    @ManyToOne
//    @JoinColumn(name = "parentUid", nullable = false)
//    private long parentAccount;

    @Column
    private String accessToken;

    @Column
    private LocalDateTime accessTokenExpiresAt;

    @Column
    private String refreshToken;

    @Column
    private LocalDateTime refreshTokenExpiresAt;

    @Column(length = 2000)
    private String idToken;

    @Column
    private String deviceInfo;


    @Builder
    public AccountKakaoToken(long parentUid, String accessToken, LocalDateTime accessTokenExpiresAt, String refreshToken, LocalDateTime refreshTokenExpiresAt, String idToken, String deviceInfo) {
        this.parentUid = parentUid;
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.idToken = idToken;
        this.deviceInfo = deviceInfo;
    }

}