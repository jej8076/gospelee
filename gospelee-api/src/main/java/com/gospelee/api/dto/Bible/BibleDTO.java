package com.gospelee.api.dto.Bible;

import lombok.Getter;

@Getter
public class BibleDTO {
    private int idx;

    private int cate;

    private int book;

    private int chapter;

    private int verse;

    private String sentence;

    private String testament;

    private String long_label;

    private String short_label;
}