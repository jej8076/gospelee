package com.gospelee.api.dto.biblereading;

import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingCalendarDTO {

  private LocalDate date;
  private int count;
  private List<String> chapters;

  @Builder
  public BibleReadingCalendarDTO(LocalDate date, int count, List<String> chapters) {
    this.date = date;
    this.count = count;
    this.chapters = chapters;
  }
}
