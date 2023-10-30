package com.gospelee.api.dto.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EditInfomationDto {
    private String insertUser;

    private String updateUser;

    private LocalDateTime insertTime;

    private LocalDateTime updateTime;
}
