package com.gospelee.api.dto.biblereading;

import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingMemberRecordDTO {

  private LocalDate date;
  // 해당 날짜에 읽은 책/장 목록 (책 번호 오름차순, 장 오름차순)
  private List<Chapter> chapters;

  @Builder
  public BibleReadingMemberRecordDTO(LocalDate date, List<Chapter> chapters) {
    this.date = date;
    this.chapters = chapters;
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Chapter {

    private int book;
    private int chapter;

    public Chapter(int book, int chapter) {
      this.book = book;
      this.chapter = chapter;
    }
  }
}
