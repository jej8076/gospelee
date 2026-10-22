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
  private List<BibleReadingGoalResponseDTO> goals;
  private int totalChapters;
  private int completedChapters;
  private double progressRate; // 0.0 ~ 100.0 (%)
  private Long daysElapsed;
  private int oldTestamentCompleted;
  private int newTestamentCompleted;
  private List<BibleReadingBookStatDTO> bookStats;
  // 선택된 목표에서 마지막으로 읽음 체크한 장 (기록이 없으면 null)
  private Integer lastReadBook;
  private Integer lastReadChapter;
  // 이어서 읽을 장: 마지막 체크 다음부터 처음 만나는 안 읽은 장 (목표를 모두 읽었거나 목표가 없으면 null)
  private Integer continueBook;
  private Integer continueChapter;

  @Builder
  public BibleReadingStatusResponseDTO(BibleReadingGoalResponseDTO activeGoal,
      List<BibleReadingGoalResponseDTO> goals, int totalChapters,
      int completedChapters, double progressRate, Long daysElapsed, int oldTestamentCompleted,
      int newTestamentCompleted, List<BibleReadingBookStatDTO> bookStats,
      Integer lastReadBook, Integer lastReadChapter, Integer continueBook,
      Integer continueChapter) {
    this.activeGoal = activeGoal;
    this.goals = goals;
    this.totalChapters = totalChapters;
    this.completedChapters = completedChapters;
    this.progressRate = progressRate;
    this.daysElapsed = daysElapsed;
    this.oldTestamentCompleted = oldTestamentCompleted;
    this.newTestamentCompleted = newTestamentCompleted;
    this.bookStats = bookStats;
    this.lastReadBook = lastReadBook;
    this.lastReadChapter = lastReadChapter;
    this.continueBook = continueBook;
    this.continueChapter = continueChapter;
  }
}
