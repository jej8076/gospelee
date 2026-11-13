package com.gospelee.api.repository.jpa.prayer;

import com.gospelee.api.entity.PrayerTreeEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrayerTreeEventRepository extends JpaRepository<PrayerTreeEvent, Long> {

  Optional<PrayerTreeEvent> findByEcclesiaUid(Long ecclesiaUid);

  void deleteByEcclesiaUid(Long ecclesiaUid);
}
