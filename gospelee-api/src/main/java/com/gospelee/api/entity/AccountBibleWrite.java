package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  @Builder
  public AccountBibleWrite(Long idx, Long accountUid, int cate, int book, int chapter) {
    this.idx = idx;
    this.accountUid = accountUid;
    this.cate = cate;
    this.book = book;
    this.chapter = chapter;
  }
}
