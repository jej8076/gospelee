package com.gospelee.api.repository.jpa.prayer;

import com.gospelee.api.entity.PrayerCardResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrayerCardResponseRepository extends JpaRepository<PrayerCardResponse, Long> {

  boolean existsByCardIdAndResponderUid(Long cardId, Long responderUid);

  List<PrayerCardResponse> findByCardId(Long cardId);

  List<PrayerCardResponse> findByInsertTimeGreaterThanEqualAndInsertTimeLessThan(
      LocalDateTime from, LocalDateTime to);

  void deleteByCardIdIn(Collection<Long> cardIds);

  void deleteByResponderUid(Long responderUid);
}
