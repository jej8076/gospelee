package com.gospelee.api.repository.jpa.reaction;

import com.gospelee.api.entity.Reaction;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

  /** 반응 취소. 지운 행 수(0 또는 1)를 돌려준다 */
  @Transactional
  @Modifying
  @Query("DELETE FROM Reaction r WHERE r.reactionType = :reactionType AND r.targetType = :targetType "
      + "AND r.targetId = :targetId AND r.accountUid = :accountUid")
  int deleteOne(@Param("reactionType") String reactionType, @Param("targetType") String targetType,
      @Param("targetId") Long targetId, @Param("accountUid") Long accountUid);

  /** 대상 하나가 사라질 때 그 대상의 반응을 모두 지운다 */
  @Transactional
  @Modifying
  @Query("DELETE FROM Reaction r WHERE r.targetType = :targetType AND r.targetId = :targetId")
  int deleteAllByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

  /** 회원 탈퇴 시 사용자의 반응을 모두 지운다 */
  @Transactional
  @Modifying
  @Query("DELETE FROM Reaction r WHERE r.accountUid = :accountUid")
  int deleteAllByAccount(@Param("accountUid") Long accountUid);

  /** 대상별 반응 수 (캐시 미스일 때 채우는 용도) */
  @Query("SELECT r.targetId, COUNT(r) FROM Reaction r WHERE r.reactionType = :reactionType "
      + "AND r.targetType = :targetType AND r.targetId IN :targetIds GROUP BY r.targetId")
  List<Object[]> countByTargets(@Param("reactionType") String reactionType,
      @Param("targetType") String targetType, @Param("targetIds") Collection<Long> targetIds);

  /** 주어진 대상 중 내가 반응한 대상 id */
  @Query("SELECT r.targetId FROM Reaction r WHERE r.accountUid = :accountUid "
      + "AND r.reactionType = :reactionType AND r.targetType = :targetType "
      + "AND r.targetId IN :targetIds")
  List<Long> findMyTargetIds(@Param("accountUid") Long accountUid,
      @Param("reactionType") String reactionType, @Param("targetType") String targetType,
      @Param("targetIds") Collection<Long> targetIds);

  /** 내가 반응한 목록 (최신순, uid 커서 기반). targetType 이 null 이면 대상 종류 무관 */
  @Query("SELECT r FROM Reaction r WHERE r.accountUid = :accountUid "
      + "AND r.reactionType = :reactionType "
      + "AND (:targetType IS NULL OR r.targetType = :targetType) "
      + "AND (:cursor IS NULL OR r.uid < :cursor) ORDER BY r.uid DESC")
  List<Reaction> findMine(@Param("accountUid") Long accountUid,
      @Param("reactionType") String reactionType, @Param("targetType") String targetType,
      @Param("cursor") Long cursor, Pageable pageable);

  /** 탈퇴 시 카운터를 무효화하기 위해, 사용자가 반응한 대상 목록 */
  @Query("SELECT r.reactionType, r.targetType, r.targetId FROM Reaction r WHERE r.accountUid = :accountUid")
  List<Object[]> findAllTargetsByAccount(@Param("accountUid") Long accountUid);
}
