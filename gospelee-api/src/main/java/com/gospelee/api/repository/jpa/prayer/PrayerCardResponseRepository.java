package com.gospelee.api.repository.jpa.prayer;

import com.gospelee.api.entity.PrayerCardResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrayerCardResponseRepository extends JpaRepository<PrayerCardResponse, Long> {

  boolean existsByCardIdAndResponderUid(Long cardId, Long responderUid);

  List<PrayerCardResponse> findByCardId(Long cardId);

  List<PrayerCardResponse> findByInsertTimeGreaterThanEqualAndInsertTimeLessThan(
      LocalDateTime from, LocalDateTime to);

  /** 내가 올린 카드에 [since] 이후 달린 기도 수 */
  @Query("select count(r) from PrayerCardResponse r where r.insertTime > :since"
      + " and r.cardId in (select c.id from PrayerCard c where c.authorUid = :authorUid)")
  long countNewOnMyCards(@Param("authorUid") Long authorUid, @Param("since") LocalDateTime since);

  void deleteByCardIdIn(Collection<Long> cardIds);

  void deleteByResponderUid(Long responderUid);
}
