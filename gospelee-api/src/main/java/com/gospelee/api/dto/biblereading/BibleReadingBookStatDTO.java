package com.gospelee.api.dto.biblereading;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingBookStatDTO {

  private int book;
  private String bookName;
  private int totalChapters;
  private int completedChapters;
  private boolean isCompleted;
  private List<Integer> completedChaptersList;

  @Builder
  public BibleReadingBookStatDTO(int book, String bookName, int totalChapters,
      int completedChapters, boolean isCompleted, List<Integer> completedChaptersList) {
    this.book = book;
    this.bookName = bookName;
    this.totalChapters = totalChapters;
    this.completedChapters = completedChapters;
    this.isCompleted = isCompleted;
    this.completedChaptersList = completedChaptersList;
  }
}
