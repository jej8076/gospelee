package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_account_bible_write", columnNames = {"account_id", "cate", "book",
        "chapter"})
})
public class AccountBibleWrite extends EditInfomation implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_bible_write_seq")
  @SequenceGenerator(name = "account_bible_write_seq", sequenceName = "account_bible_write_seq", allocationSize = 1)
  @Column
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