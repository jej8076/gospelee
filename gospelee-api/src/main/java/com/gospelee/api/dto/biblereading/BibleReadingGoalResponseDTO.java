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
  private String orderType;
  private List<Integer> customBooks;
  private LocalDate startDate;
  private LocalDate targetDate;
  private Integer targetDays;
  private int totalChapters;
  private String status;
  private long daysElapsed;
  private int completedChapters;
  private double progressRate;
  private String inviteCode;
  private int participantCount;
  private Boolean isHost;

  @Builder
  public BibleReadingGoalResponseDTO(Long idx, String title, String rangeType, String orderType,
      List<Integer> customBooks, LocalDate startDate, LocalDate targetDate, Integer targetDays,
      int totalChapters, String status, long daysElapsed, int completedChapters, double progressRate,
      String inviteCode, int participantCount, Boolean isHost) {
    this.idx = idx;
    this.title = title;
    this.rangeType = rangeType;
    this.orderType = orderType;
    this.customBooks = customBooks;
    this.startDate = startDate;
    this.targetDate = targetDate;
    this.targetDays = targetDays;
    this.totalChapters = totalChapters;
    this.status = status;
    this.daysElapsed = daysElapsed;
    this.completedChapters = completedChapters;
    this.progressRate = progressRate;
    this.inviteCode = inviteCode;
    this.participantCount = participantCount;
    this.isHost = isHost != null ? isHost : true;
  }

  public static BibleReadingGoalResponseDTO fromEntity(AccountBibleReadingGoal entity) {
    return fromEntity(entity, 0, 0.0, 1, true);
  }

  public static BibleReadingGoalResponseDTO fromEntity(AccountBibleReadingGoal entity,
      int completedChapters, double progressRate) {
    return fromEntity(entity, completedChapters, progressRate, 1, true);
  }

  public static BibleReadingGoalResponseDTO fromEntity(AccountBibleReadingGoal entity,
      int completedChapters, double progressRate, int participantCount, boolean isHost) {
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
        .orderType(entity.getOrderType() != null ? entity.getOrderType() : "CANONICAL")
        .customBooks(customBooksList)
        .startDate(entity.getStartDate())
        .targetDate(entity.getTargetDate())
        .targetDays(entity.getTargetDays())
        .totalChapters(entity.getTotalChapters())
        .status(entity.getStatus())
        .daysElapsed(days)
        .completedChapters(completedChapters)
        .progressRate(progressRate)
        .inviteCode(entity.getInviteCode())
        .participantCount(participantCount > 0 ? participantCount : 1)
        .isHost(isHost)
        .build();
  }
}
