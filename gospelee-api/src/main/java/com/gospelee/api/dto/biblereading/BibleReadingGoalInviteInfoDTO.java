package com.gospelee.api.dto.biblereading;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingGoalInviteInfoDTO {

  private Long goalIdx;
  private String title;
  private String rangeType;
  private String rangeTypeLabel;
  private String orderType;
  private String orderTypeLabel;
  private LocalDate startDate;
  private LocalDate targetDate;
  private Integer targetDays;
  private int totalChapters;
  private int participantCount;
  private String hostName;
  private String inviteCode;
  private boolean isAlreadyJoined;

  @Builder
  public BibleReadingGoalInviteInfoDTO(Long goalIdx, String title, String rangeType,
      String rangeTypeLabel, String orderType, String orderTypeLabel, LocalDate startDate,
      LocalDate targetDate, Integer targetDays, int totalChapters, int participantCount,
      String hostName, String inviteCode, boolean isAlreadyJoined) {
    this.goalIdx = goalIdx;
    this.title = title;
    this.rangeType = rangeType;
    this.rangeTypeLabel = rangeTypeLabel;
    this.orderType = orderType;
    this.orderTypeLabel = orderTypeLabel;
    this.startDate = startDate;
    this.targetDate = targetDate;
    this.targetDays = targetDays;
    this.totalChapters = totalChapters;
    this.participantCount = participantCount;
    this.hostName = hostName;
    this.inviteCode = inviteCode;
    this.isAlreadyJoined = isAlreadyJoined;
  }
}
