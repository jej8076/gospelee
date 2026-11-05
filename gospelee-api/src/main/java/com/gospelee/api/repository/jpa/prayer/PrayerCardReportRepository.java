package com.gospelee.api.repository.jpa.prayer;

import com.gospelee.api.entity.PrayerCardReport;
import com.gospelee.api.enums.PrayerEnums.ReportStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrayerCardReportRepository extends JpaRepository<PrayerCardReport, Long> {

  boolean existsByCardIdAndReporterUid(Long cardId, Long reporterUid);

  List<PrayerCardReport> findByCardIdAndStatus(Long cardId, ReportStatus status);

  List<PrayerCardReport> findByCardIdIn(Collection<Long> cardIds);

  void deleteByCardIdIn(Collection<Long> cardIds);

  void deleteByReporterUid(Long reporterUid);
}
