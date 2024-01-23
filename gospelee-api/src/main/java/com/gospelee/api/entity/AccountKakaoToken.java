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
    /*
    accessToken = "_OsMmILbWx-QoyAksKKWDyW4HT-7M2iYgcUKPXTbAAABjOxgGyOSBpCp5rpDbg"
    expiresAt = {DateTime} 2024-01-10 00:59:26.881
    refreshToken = "9rs4OXuYWRyBT4FnGSteBz4QIxggayxkSN0KPXTbAAABjOxgGx-SBpCp5rpDbg"
    refreshTokenExpiresAt = {DateTime} 2024-03-09 12:59:26.881
    scopes = {_GrowableList} size = 2
    idToken = "eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJiYzlhYzBmZDRjZDE3YTg1OGM5NzF"
     */
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