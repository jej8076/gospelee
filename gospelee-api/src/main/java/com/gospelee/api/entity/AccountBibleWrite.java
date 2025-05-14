package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleWrite extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long idx;

  @Column(name = "account_id")
  private long accountUid;

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

}