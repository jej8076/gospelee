package com.gospelee.api.dto;

import com.gospelee.api.annotation.validation.PhoneNumber;
import com.gospelee.api.annotation.validation.RRN;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class BibleVo {
    private int idx;

    private int cate;

    private int book;

    private int chapter;

    private int paragraph;

    private String sentence;

    private String testament;

    private String long_label;

    private String short_label;
}