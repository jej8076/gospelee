package com.gospelee.api.repository.jpa.journal;

import com.gospelee.api.entity.ContentReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

  boolean existsByReporterUidAndTargetTypeAndTargetUid(Long reporterUid, String targetType,
      Long targetUid);

  List<ContentReport> findByStatusOrderByUidDesc(String status);

  List<ContentReport> findByTargetTypeAndTargetUidAndStatus(String targetType, Long targetUid,
      String status);
}
