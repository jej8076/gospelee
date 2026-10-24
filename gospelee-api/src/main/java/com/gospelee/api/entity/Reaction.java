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

/**
 * 사용자가 어떤 대상에 남긴 반응 한 건 (예: 공지사항 N 에 "기도할게요").
 * 같은 사용자는 같은 대상에 같은 반응을 한 번만 남길 수 있다(유니크 제약).
 * 행 insert 는 {@code ReactionRepository#insertIgnore} 로만 한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reaction", uniqueConstraints = @UniqueConstraint(
    name = "uk_reaction", columnNames = {"reactionType", "targetType", "targetId", "accountUid"}))
public class Reaction extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column(nullable = false, length = 30)
  private String reactionType;

  @Column(nullable = false, length = 30)
  private String targetType;

  @Column(nullable = false)
  private Long targetId;

  @Column(nullable = false)
  private Long accountUid;
}
