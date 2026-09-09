package com.gospelee.api.dto.biblereading;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BibleReadingMemberDTO {

  private Long accountUid;
  private String name;
  private String role; // HOST, MEMBER
  private int completedChapters;
  private double progressRate;
  private LocalDate lastReadDate;
  private LocalDateTime joinedAt;
  private boolean isMe;

  @Builder
  public BibleReadingMemberDTO(Long accountUid, String name, String role, int completedChapters,
      double progressRate, LocalDate lastReadDate, LocalDateTime joinedAt, boolean isMe) {
    this.accountUid = accountUid;
    this.name = name;
    this.role = role;
    this.completedChapters = completedChapters;
    this.progressRate = progressRate;
    this.lastReadDate = lastReadDate;
    this.joinedAt = joinedAt;
    this.isMe = isMe;
  }
}
