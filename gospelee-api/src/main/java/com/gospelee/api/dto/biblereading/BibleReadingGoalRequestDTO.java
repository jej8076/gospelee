package com.gospelee.api.dto.biblereading;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingGoalRequestDTO {

  @NotBlank(message = "목표 제목을 입력해주세요.")
  private String title;

  // ALL, OLD, NEW, CUSTOM
  @NotBlank(message = "목표 범위를 선택해주세요.")
  private String rangeType;

  // CUSTOM일 때 선택한 책 번호 목록 (예: [1, 2, 3])
  private List<Integer> customBooks;

  private LocalDate startDate;

  // 목표 종료일 (종료일 지정 방식일 경우)
  private LocalDate targetDate;

  // 목표 일수 (일수 지정 방식일 경우)
  private Integer targetDays;

  @Builder
  public BibleReadingGoalRequestDTO(String title, String rangeType, List<Integer> customBooks,
      LocalDate startDate, LocalDate targetDate, Integer targetDays) {
    this.title = title;
    this.rangeType = rangeType;
    this.customBooks = customBooks;
    this.startDate = startDate != null ? startDate : LocalDate.now();
    this.targetDate = targetDate;
    this.targetDays = targetDays;
  }
}
