package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @Column
    private String accessToken;

    @Column
    private String refreshToken;

    @Column
    private String deviceInfo;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private Account parentAccount;
}