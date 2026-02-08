package com.gospelee.api.dto.bible;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookStatDTO {
  private int book;              // 1-66
  private String bookName;       // "창세기"
  private int totalChapters;     // 해당 책의 총 장 수
  private int completedChapters; // 완료한 장 수
}
