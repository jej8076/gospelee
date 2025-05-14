package com.gospelee.api.dto.bible;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleWriteDTO {

  private String phone;

  // 구약 = 1, 신약 = 2
  private int cate;

  private int book;

  // N장
  private int chapter;

  @Builder
  public AccountBibleWriteDTO(String phone, int cate, int book, int chapter) {
    this.phone = phone;
    this.cate = cate;
    this.book = book;
    this.chapter = chapter;
  }
}
