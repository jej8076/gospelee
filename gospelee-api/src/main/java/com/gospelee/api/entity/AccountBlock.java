package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 차단: blocker 는 blocked 의 공유와 댓글을 보지 않는다 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_block", uniqueConstraints = @UniqueConstraint(
    name = "uk_account_block", columnNames = {"blockerUid", "blockedUid"}))
public class AccountBlock extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column(nullable = false)
  private Long blockerUid;

  @Column(nullable = false)
  private Long blockedUid;

  public AccountBlock(Long blockerUid, Long blockedUid) {
    this.blockerUid = blockerUid;
    this.blockedUid = blockedUid;
  }
}
