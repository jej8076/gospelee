package com.gospelee.api.dto;

import com.gospelee.api.annotation.validation.PhoneNumber;
import com.gospelee.api.annotation.validation.RRN;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class AccountVo {

    @NotEmpty(message = "이름 정도는 알려줄 수 있잖아?")
    private String name;

    @RRN
    @NotEmpty(message = "주민번호도 필수임 ㅋ")
    private String rrn;

    @PhoneNumber
    @NotEmpty(message = "핸드폰 번호는 필수 값입니다.. (엄 근 진)")
    private String phone;

    private void setRrn(String rrn) {
        this.rrn = rrn.replace("-", "");
    }
}