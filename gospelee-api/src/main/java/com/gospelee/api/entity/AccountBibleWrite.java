package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleWrite extends EditInfomation {

    @Id
    @Column
    private int idx;

    @Column
    private long accountUid;

    // 구약 = 1, 신약 = 2
    @Column
    private int cate;

    @Column
    private int book;

    // N장
    @Column
    private int chapter;

    // 읽은 횟수
    @Column
    @ColumnDefault("1")
    private int count;

}