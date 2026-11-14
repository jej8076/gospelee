package com.gospelee.api.service.ministry;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.entity.ContentReport;
import com.gospelee.api.entity.Ministry;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.repository.jpa.journal.ContentReportRepository;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 모임 공지 신고. 그 모임의 활성 멤버만 신고할 수 있고, 처리는 관리자 신고 관리에서 한다 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinistryReportService {

  private static final int DETAIL_MAX_LENGTH = 300;
  private static final Set<String> REASONS = Set.of("SPAM", "ABUSE", "SEXUAL", "HERESY",
      "PRIVACY", "OTHER");

  private final MinistryService ministryService;
  private final AnnouncementRepository announcementRepository;
  private final ContentReportRepository reportRepository;

  @Transactional
  public void report(AccountAuthDTO account, Long ministryId, Long announcementId, String reason,
      String detail) {
    // 멤버가 아니면 모임이 있는지도 알 수 없는 응답
    Ministry ministry = ministryService.requireMemberOf(ministryId, account.getUid());
    if (reason == null || !REASONS.contains(reason)) {
      throw new IllegalArgumentException("신고 사유를 선택해주세요.");
    }
    // 다른 모임의 공지를 이 모임 경로로 신고하지 못하게 소속을 확인한다
    Announcement announcement = announcementRepository.findById(announcementId)
        .filter(a -> OrganizationType.MINISTRY.name().equals(a.getOrganizationType())
            && ministryId.equals(a.getOrganizationId()))
        .orElseThrow(() -> new NoSuchElementException("신고할 공지를 찾을 수 없습니다."));
    if (reportRepository.existsByReporterUidAndTargetTypeAndTargetUid(account.getUid(),
        ContentReport.TARGET_MINISTRY_ANNOUNCEMENT, announcement.getId())) {
      throw new IllegalArgumentException("이미 신고한 공지입니다.");
    }

    String safeDetail = detail == null || detail.isBlank() ? null : detail.trim();
    if (safeDetail != null && safeDetail.length() > DETAIL_MAX_LENGTH) {
      safeDetail = safeDetail.substring(0, DETAIL_MAX_LENGTH);
    }
    reportRepository.save(new ContentReport(account.getUid(),
        ContentReport.TARGET_MINISTRY_ANNOUNCEMENT, announcement.getId(), reason, safeDetail));
    log.warn("[REPORT    ] reporter:{} type:MIN_ANN target:{} ministry:{} reason:{}",
        account.getUid(), announcement.getId(), ministryId, reason);

    // 같은 공지에 신고가 쌓여도 알림은 첫 신고에서 한 번만 보낸다
    if (reportRepository.countByTargetTypeAndTargetUid(
        ContentReport.TARGET_MINISTRY_ANNOUNCEMENT, announcement.getId()) == 1) {
      try {
        ministryService.notifyManagersOfReport(ministry);
      } catch (Exception e) {
        log.warn("[REPORT    ] 모임 운영진 알림 실패 ministry:{}", ministryId, e);
      }
    }
  }
}
