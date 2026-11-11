package com.gospelee.api.service.ministry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.entity.ContentReport;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.repository.jpa.journal.ContentReportRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class MinistryReportServiceTest {

  private MinistryService ministryService;
  private AnnouncementRepository announcementRepository;
  private ContentReportRepository reportRepository;
  private MinistryReportService service;
  private final AccountAuthDTO reporter = AccountAuthDTO.builder().uid(5L).build();

  @BeforeEach
  void setUp() {
    ministryService = mock(MinistryService.class);
    announcementRepository = mock(AnnouncementRepository.class);
    reportRepository = mock(ContentReportRepository.class);
    service = new MinistryReportService(ministryService, announcementRepository, reportRepository);

    Announcement announcement = Announcement.builder()
        .organizationType(OrganizationType.MINISTRY.name()).organizationId(3L).subject("제목")
        .text("본문").build();
    ReflectionTestUtils.setField(announcement, "id", 9L);
    when(announcementRepository.findById(9L)).thenReturn(Optional.of(announcement));
  }

  @Test
  void 멤버가_신고하면_모임_공지_신고로_저장된다() {
    service.report(reporter, 3L, 9L, "SPAM", " 광고예요 ");

    ArgumentCaptor<ContentReport> saved = ArgumentCaptor.forClass(ContentReport.class);
    verify(reportRepository).save(saved.capture());
    assertEquals(ContentReport.TARGET_MINISTRY_ANNOUNCEMENT, saved.getValue().getTargetType());
    assertEquals(9L, saved.getValue().getTargetUid());
    assertEquals("광고예요", saved.getValue().getDetail());
  }

  @Test
  void 멤버가_아니면_신고할_수_없다() {
    when(ministryService.requireMemberOf(3L, 5L)).thenThrow(new NoSuchElementException("없음"));
    assertThrows(NoSuchElementException.class, () -> service.report(reporter, 3L, 9L, "SPAM", null));
    verify(reportRepository, never()).save(any());
  }

  @Test
  void 다른_모임의_공지는_이_모임_경로로_신고할_수_없다() {
    assertThrows(NoSuchElementException.class, () -> service.report(reporter, 4L, 9L, "SPAM", null));
    verify(reportRepository, never()).save(any());
  }

  @Test
  void 사유가_잘못되거나_이미_신고했으면_거절한다() {
    assertThrows(IllegalArgumentException.class, () -> service.report(reporter, 3L, 9L, "???", null));
    when(reportRepository.existsByReporterUidAndTargetTypeAndTargetUid(5L,
        ContentReport.TARGET_MINISTRY_ANNOUNCEMENT, 9L)).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> service.report(reporter, 3L, 9L, "SPAM", null));
    verify(reportRepository, never()).save(any());
  }
}
