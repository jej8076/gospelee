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

}