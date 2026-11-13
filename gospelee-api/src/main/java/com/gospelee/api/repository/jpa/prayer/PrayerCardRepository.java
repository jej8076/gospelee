package com.gospelee.api.repository.jpa.prayer;

import com.gospelee.api.entity.PrayerCard;
import com.gospelee.api.enums.PrayerEnums.CardScope;
import com.gospelee.api.enums.PrayerEnums.CardStatus;
import com.gospelee.api.enums.PrayerEnums.ReportStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrayerCardRepository extends JpaRepository<PrayerCard, Long> {

  Optional<PrayerCard> findByIdAndEcclesiaUid(Long id, Long ecclesiaUid);

  List<PrayerCard> findByAuthorUidOrderByIdDesc(Long authorUid);

  List<PrayerCard> findByEcclesiaUidAndStatusOrderByIdAsc(Long ecclesiaUid, CardStatus status);

  /**
   * 뽑을 수 있는 카드: 내 교회, 노출 중, 만료 전, 내가 쓰지 않았고, 내가 아직 기도하지 않았고,
   * 내가 차단한 사람의 글이 아니며, 그룹 카드는 내가 그 그룹 멤버일 때만. 기도를 덜 받은 카드가 먼저 온다.
   */
  @Query("select c from PrayerCard c where c.ecclesiaUid = :ecclesiaUid"
      + " and c.status = :active"
      + " and (c.expiresAt is null or c.expiresAt > :now)"
      + " and c.authorUid <> :me"
      + " and not exists (select 1 from PrayerCardResponse r"
      + "   where r.cardId = c.id and r.responderUid = :me)"
      + " and not exists (select 1 from AccountBlock b"
      + "   where b.blockerUid = :me and b.blockedUid = c.authorUid)"
      + " and (c.scope = :churchScope or c.groupId in"
      + "   (select m.groupId from PushGroupMember m where m.accountUid = :me))"
      + " order by c.responseCount asc, c.id asc")
  List<PrayerCard> findDrawCandidates(@Param("ecclesiaUid") Long ecclesiaUid,
      @Param("me") Long me, @Param("now") LocalDateTime now,
      @Param("active") CardStatus active, @Param("churchScope") CardScope churchScope,
      Pageable pageable);

  /** [findDrawCandidates] 와 같은 조건으로 센 뽑을 수 있는 카드 수 */
  @Query("select count(c) from PrayerCard c where c.ecclesiaUid = :ecclesiaUid"
      + " and c.status = :active"
      + " and (c.expiresAt is null or c.expiresAt > :now)"
      + " and c.authorUid <> :me"
      + " and not exists (select 1 from PrayerCardResponse r"
      + "   where r.cardId = c.id and r.responderUid = :me)"
      + " and not exists (select 1 from AccountBlock b"
      + "   where b.blockerUid = :me and b.blockedUid = c.authorUid)"
      + " and (c.scope = :churchScope or c.groupId in"
      + "   (select m.groupId from PushGroupMember m where m.accountUid = :me))")
  long countDrawable(@Param("ecclesiaUid") Long ecclesiaUid, @Param("me") Long me,
      @Param("now") LocalDateTime now, @Param("active") CardStatus active,
      @Param("churchScope") CardScope churchScope);

  /** 신고가 처리 대기 중인 카드 */
  @Query("select c from PrayerCard c where c.ecclesiaUid = :ecclesiaUid"
      + " and exists (select 1 from PrayerCardReport r"
      + "   where r.cardId = c.id and r.status = :pending)"
      + " order by c.id desc")
  List<PrayerCard> findWithPendingReports(@Param("ecclesiaUid") Long ecclesiaUid,
      @Param("pending") ReportStatus pending);

  /** 트리에 걸 카드: 교회 전체에 공개된 노출 중 카드, 최신순 */
  @Query("select c from PrayerCard c where c.ecclesiaUid = :ecclesiaUid"
      + " and c.status = :active and c.scope = :churchScope"
      + " and (c.expiresAt is null or c.expiresAt > :now)"
      + " order by c.id desc")
  List<PrayerCard> findTreeCards(@Param("ecclesiaUid") Long ecclesiaUid,
      @Param("now") LocalDateTime now, @Param("active") CardStatus active,
      @Param("churchScope") CardScope churchScope, Pageable pageable);

  @Query("select count(c) from PrayerCard c where c.ecclesiaUid = :ecclesiaUid"
      + " and c.status = :active and c.scope = :churchScope"
      + " and (c.expiresAt is null or c.expiresAt > :now)")
  long countTreeCards(@Param("ecclesiaUid") Long ecclesiaUid,
      @Param("now") LocalDateTime now, @Param("active") CardStatus active,
      @Param("churchScope") CardScope churchScope);

  @Query("select count(c) from PrayerCard c where c.ecclesiaUid = :ecclesiaUid"
      + " and c.status = :active and c.scope = :churchScope and c.responseCount > 0"
      + " and (c.expiresAt is null or c.expiresAt > :now)")
  long countLitTreeCards(@Param("ecclesiaUid") Long ecclesiaUid,
      @Param("now") LocalDateTime now, @Param("active") CardStatus active,
      @Param("churchScope") CardScope churchScope);

  @Modifying
  @Query("update PrayerCard c set c.responseCount = c.responseCount + 1 where c.id = :id")
  void increaseResponseCount(@Param("id") Long id);

  // 통계 (개인 식별 정보 없이 집계만)
  long countByEcclesiaUid(Long ecclesiaUid);

  List<PrayerCard> findByEcclesiaUid(Long ecclesiaUid);

  long countByEcclesiaUidAndStatus(Long ecclesiaUid, CardStatus status);

  @Query("select count(c) from PrayerCard c where c.ecclesiaUid = :ecclesiaUid"
      + " and c.status = :active and c.responseCount = 0"
      + " and (c.expiresAt is null or c.expiresAt > :now)")
  long countOpenWithoutResponse(@Param("ecclesiaUid") Long ecclesiaUid,
      @Param("now") LocalDateTime now, @Param("active") CardStatus active);

  @Query("select coalesce(sum(c.responseCount), 0) from PrayerCard c"
      + " where c.ecclesiaUid = :ecclesiaUid")
  long sumResponseCount(@Param("ecclesiaUid") Long ecclesiaUid);

  List<PrayerCard> findByAuthorUid(Long authorUid);
}
