package com.gospelee.api.dto.biblereading;

import com.gospelee.api.entity.AccountBibleReadingGoal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingGoalResponseDTO {

  private Long idx;
  private String title;
  private String rangeType;
  private List<Integer> customBooks;
  private LocalDate startDate;
  private LocalDate targetDate;
  private Integer targetDays;
  private int totalChapters;
  private String status;
  private long daysElapsed;

  @Builder
  public BibleReadingGoalResponseDTO(Long idx, String title, String rangeType,
      List<Integer> customBooks, LocalDate startDate, LocalDate targetDate, Integer targetDays,
      int totalChapters, String status, long daysElapsed) {
    this.idx = idx;
    this.title = title;
    this.rangeType = rangeType;
    this.customBooks = customBooks;
    this.startDate = startDate;
    this.targetDate = targetDate;
    this.targetDays = targetDays;
    this.totalChapters = totalChapters;
    this.status = status;
    this.daysElapsed = daysElapsed;
  }

  public static BibleReadingGoalResponseDTO fromEntity(AccountBibleReadingGoal entity) {
    if (entity == null) {
      return null;
    }

    List<Integer> customBooksList = new ArrayList<>();
    if (entity.getCustomBooks() != null && !entity.getCustomBooks().isBlank()) {
      customBooksList = Arrays.stream(entity.getCustomBooks().split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
    }

    LocalDate now = LocalDate.now();
    long days = 0;
    if (entity.getStartDate() != null) {
      days = ChronoUnit.DAYS.between(entity.getStartDate(), now) + 1;
      if (days < 1) days = 1;
    }

    return BibleReadingGoalResponseDTO.builder()
        .idx(entity.getIdx())
        .title(entity.getTitle())
        .rangeType(entity.getRangeType())
        .customBooks(customBooksList)
        .startDate(entity.getStartDate())
        .targetDate(entity.getTargetDate())
        .targetDays(entity.getTargetDays())
        .totalChapters(entity.getTotalChapters())
        .status(entity.getStatus())
        .daysElapsed(days)
        .build();
  }
}
