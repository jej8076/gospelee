package com.gospelee.api.entity;


import annotation.validation.PhoneNumber;
import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends EditInfomation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column()
    private long uid;

    @Column
    private String id;

    @NotEmpty(message = "이름 정도는 알려줄 수 있잖아?")
    @Column
    private String name;

    @NotEmpty(message = "주민번호도 필수임 ㅋ")
    @Column
    private String rrn;

    @NotEmpty(message = "핸드폰 번호는 필수 값입니다.. (엄 근 진)")
    @Column
    private String phone;

//    @Builder
//    public Account(String id, String name, String phone){
//        this.id = id;
//        this.name = name;
//        this.phone = phone;
//    }

}