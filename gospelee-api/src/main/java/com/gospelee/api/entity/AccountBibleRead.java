package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "account_bible_read",
    indexes = {
        @Index(name = "idx_abr_account_book_chapter", columnList = "account_id, book, chapter"),
        @Index(name = "idx_abr_account_read_date", columnList = "account_id, read_date")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleRead extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long idx;

  @Column(name = "account_id", nullable = false)
  private Long accountUid;

  @Column(name = "goal_id")
  private Long goalIdx;

  // 구약 = 1, 신약 = 2
  @Column(nullable = false)
  private int cate;

  // 성경 1~66
  @Column(nullable = false)
  private int book;

  // N장
  @Column(nullable = false)
  private int chapter;

  // 읽은 일자 (캘린더 연동용)
  @Column(name = "read_date", nullable = false)
  private LocalDate readDate;

  @Builder
  public AccountBibleRead(Long idx, Long accountUid, Long goalIdx, int cate, int book,
      int chapter, LocalDate readDate) {
    this.idx = idx;
    this.accountUid = accountUid;
    this.goalIdx = goalIdx;
    this.cate = cate;
    this.book = book;
    this.chapter = chapter;
    this.readDate = readDate != null ? readDate : LocalDate.now();
  }
}
