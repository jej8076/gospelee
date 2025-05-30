package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleWrite extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long idx;

  @Column(name = "account_id")
  private Long accountUid;

  // 구약 = 1, 신약 = 2
  @Column
  private int cate;

  @Column
  private int book;

  // N장
  @Column
  private int chapter;

  // 읽은 횟수
  @Column
  @ColumnDefault("1")
  private int count;

  @Builder
  public AccountBibleWrite(Long idx, Long accountUid, int cate, int book, int chapter, int count) {
    this.idx = idx;
    this.accountUid = accountUid;
    this.cate = cate;
    this.book = book;
    this.chapter = chapter;
    this.count = count;
  }
}
