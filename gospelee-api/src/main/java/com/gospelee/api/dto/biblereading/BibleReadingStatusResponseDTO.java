package com.gospelee.api.dto.biblereading;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingStatusResponseDTO {

  private BibleReadingGoalResponseDTO activeGoal;
  private int totalChapters;
  private int completedChapters;
  private double progressRate; // 0.0 ~ 100.0 (%)
  private Long daysElapsed;
  private int oldTestamentCompleted;
  private int newTestamentCompleted;
  private List<BibleReadingBookStatDTO> bookStats;

  @Builder
  public BibleReadingStatusResponseDTO(BibleReadingGoalResponseDTO activeGoal, int totalChapters,
      int completedChapters, double progressRate, Long daysElapsed, int oldTestamentCompleted,
      int newTestamentCompleted, List<BibleReadingBookStatDTO> bookStats) {
    this.activeGoal = activeGoal;
    this.totalChapters = totalChapters;
    this.completedChapters = completedChapters;
    this.progressRate = progressRate;
    this.daysElapsed = daysElapsed;
    this.oldTestamentCompleted = oldTestamentCompleted;
    this.newTestamentCompleted = newTestamentCompleted;
    this.bookStats = bookStats;
  }
}
