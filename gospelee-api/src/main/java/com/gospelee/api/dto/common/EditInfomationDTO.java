package com.gospelee.api.dto.common;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EditInfomationDTO {
    private String insertUser;

    private String updateUser;

    private LocalDateTime insertTime;

    private LocalDateTime updateTime;
}
