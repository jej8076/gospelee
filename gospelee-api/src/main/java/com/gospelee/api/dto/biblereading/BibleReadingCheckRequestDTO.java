package com.gospelee.api.dto.biblereading;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingCheckRequestDTO {

  @NotNull(message = "성경 책 번호를 지정해주세요.")
  private Integer book;

  @NotEmpty(message = "선택할 장 목록을 지정해주세요.")
  private List<Integer> chapters;

  // "READ" 또는 "UNREAD"
  @NotNull(message = "액션을 지정해주세요. (READ / UNREAD)")
  private String action;

  // 캘린더 읽은 일자 (null이면 오늘)
  private LocalDate readDate;

  @Builder
  public BibleReadingCheckRequestDTO(Integer book, List<Integer> chapters, String action,
      LocalDate readDate) {
    this.book = book;
    this.chapters = chapters;
    this.action = action;
    this.readDate = readDate;
  }
}
