package com.gospelee.api.repository.jpa.prayer;

import com.gospelee.api.entity.PrayerCardDraw;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrayerCardDrawRepository extends JpaRepository<PrayerCardDraw, Long> {

  long countByDrawerUidAndDrawnAtGreaterThanEqual(Long drawerUid, LocalDateTime since);

  void deleteByCardIdIn(Collection<Long> cardIds);

  void deleteByDrawerUid(Long drawerUid);
}
