package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleWrite extends EditInfomation {

    @Id
    @Column
    private int idx;

    @Column
    @ManyToOne()
    private long accountUid;

    @Column
    private int book;

    @Column
    private int chapter;

    @Column
    private int verse;

    @Column
    private String sentence;

    @Column
    private String testament;

    @Column(name = "long_label")
    private String longLabel;

    @Column(name = "short_label")
    private String shortLabel;

    @Column(name = "long_label_eng")
    private String longLabelEng;

    @Column(name = "short_label_eng")
    private String shortLabelEng;

}