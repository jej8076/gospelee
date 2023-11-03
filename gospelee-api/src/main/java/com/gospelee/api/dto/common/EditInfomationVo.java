package com.gospelee.api.dto.common;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EditInfomationVo {
    private String insertUser;

    private String updateUser;

    private LocalDateTime insertTime;

    private LocalDateTime updateTime;
}
