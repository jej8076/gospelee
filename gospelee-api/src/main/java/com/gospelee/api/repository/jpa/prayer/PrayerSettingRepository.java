package com.gospelee.api.repository.jpa.prayer;

import com.gospelee.api.entity.PrayerSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrayerSettingRepository extends JpaRepository<PrayerSetting, Long> {

  Optional<PrayerSetting> findByEcclesiaUid(Long ecclesiaUid);

  void deleteByEcclesiaUid(Long ecclesiaUid);
}
