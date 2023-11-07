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
public class Bible extends EditInfomation {

    @Id
    @Column
    private int idx;

    @Column
    private int cate;

    @Column
    private int book;

    @Column
    private int chapter;

    @Column
    private int paragraph;

    @Column
    private String sentence;

    @Column
    private String testament;

    @Column(name = "long_label")
    private String longLabel;

    @Column(name = "short_label")
    private String shortLabel;

}