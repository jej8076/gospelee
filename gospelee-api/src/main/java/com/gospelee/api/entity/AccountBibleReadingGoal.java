package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "account_bible_reading_goal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleReadingGoal extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long idx;

  @Column(name = "account_id", nullable = false)
  private Long accountUid;

  @Column(nullable = false, length = 100)
  private String title;

  // ALL, OLD, NEW, CUSTOM
  @Column(name = "range_type", nullable = false, length = 20)
  private String rangeType;

  // CUSTOM일 때 선택된 책 번호들 (예: "1,2,3")
  @Column(name = "custom_books", length = 1000)
  private String customBooks;

  // CANONICAL(기본 정경순), CHRONOLOGICAL(연대기순), NEW_FIRST(신약 우선), CUSTOM(직접 지정)
  @Column(name = "order_type", length = 30)
  private String orderType;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  // 목표 종료일 (자유 통독일 경우 null 가능)
  @Column(name = "target_date")
  private LocalDate targetDate;

  // 목표 일수 (예: 100, 365 등, 자유 통독일 경우 null 가능)
  @Column(name = "target_days")
  private Integer targetDays;

  // 목표 총 장 수 (ALL=1189, OLD=929, NEW=260, CUSTOM=선택된 책들의 장 수 합)
  @Column(name = "total_chapters", nullable = false)
  private int totalChapters;

  // PROGRESS, COMPLETED, CANCELED
  @Column(nullable = false, length = 20)
  private String status;

  // 초대/공유용 고유 코드 (12자리 영숫자)
  @Column(name = "invite_code", unique = true, length = 32)
  private String inviteCode;

  @Builder
  public AccountBibleReadingGoal(Long idx, Long accountUid, String title, String rangeType,
      String customBooks, String orderType, LocalDate startDate, LocalDate targetDate, Integer targetDays,
      int totalChapters, String status, String inviteCode) {
    this.idx = idx;
    this.accountUid = accountUid;
    this.title = title;
    this.rangeType = rangeType;
    this.customBooks = customBooks;
    this.orderType = orderType != null ? orderType : "CANONICAL";
    this.startDate = startDate;
    this.targetDate = targetDate;
    this.targetDays = targetDays;
    this.totalChapters = totalChapters;
    this.status = status != null ? status : "PROGRESS";
    this.inviteCode = inviteCode;
  }

  public void complete() {
    this.status = "COMPLETED";
  }

  public void cancel() {
    this.status = "CANCELED";
  }

  public void changeInviteCode(String inviteCode) {
    this.inviteCode = inviteCode;
  }

  public void changeAccountUid(Long accountUid) {
    this.accountUid = accountUid;
  }
}
