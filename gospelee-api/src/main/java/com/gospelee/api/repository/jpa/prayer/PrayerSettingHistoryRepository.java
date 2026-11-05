package com.gospelee.api.repository.jpa.prayer;

import com.gospelee.api.entity.PrayerSettingHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrayerSettingHistoryRepository extends JpaRepository<PrayerSettingHistory, Long> {

  List<PrayerSettingHistory> findTop20ByEcclesiaUidOrderByChangedAtDesc(Long ecclesiaUid);
}
