package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.entity.ContentReport;
import com.gospelee.api.entity.JournalShare;
import com.gospelee.api.entity.JournalShareComment;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.journal.ContentReportRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareCommentRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminReportServiceTest {

  private ContentReportRepository reportRepository;
  private JournalShareRepository shareRepository;
  private JournalShareCommentRepository commentRepository;
  private AdminReportService service;

  @BeforeEach
  void setUp() {
    reportRepository = mock(ContentReportRepository.class);
    shareRepository = mock(JournalShareRepository.class);
    commentRepository = mock(JournalShareCommentRepository.class);
    service = new AdminReportService(reportRepository, shareRepository, commentRepository,
        mock(AccountRepository.class));
  }

  @Test
  void 댓글_신고를_내리면_댓글이_삭제되고_같은_대상의_대기_신고가_모두_처리된다() {
    ContentReport first = new ContentReport(1L, ContentReport.TARGET_COMMENT, 7L, "SPAM", null);
    ContentReport second = new ContentReport(2L, ContentReport.TARGET_COMMENT, 7L, "ABUSE", null);
    JournalShareComment comment = new JournalShareComment(3L, 4L, "나쁜 댓글");
    when(reportRepository.findById(10L)).thenReturn(Optional.of(first));
    when(commentRepository.findById(7L)).thenReturn(Optional.of(comment));
    when(reportRepository.findByTargetTypeAndTargetUidAndStatus(ContentReport.TARGET_COMMENT, 7L,
        ContentReport.PENDING)).thenReturn(List.of(first, second));

    service.resolve(10L, AdminReportService.ACTION_TAKEDOWN, 99L);

    assertFalse(comment.isActive());
    assertEquals(ContentReport.RESOLVED, first.getStatus());
    assertEquals(ContentReport.RESOLVED, second.getStatus());
  }

  @Test
  void 공유_신고를_내리면_공유가_취소된다() {
    ContentReport report = new ContentReport(1L, ContentReport.TARGET_SHARE, 5L, "SPAM", null);
    JournalShare share = new JournalShare(1L, 2L, "token", "요 3:16", "구절", "묵상");
    when(reportRepository.findById(10L)).thenReturn(Optional.of(report));
    when(shareRepository.findById(5L)).thenReturn(Optional.of(share));
    when(reportRepository.findByTargetTypeAndTargetUidAndStatus(ContentReport.TARGET_SHARE, 5L,
        ContentReport.PENDING)).thenReturn(List.of(report));

    service.resolve(10L, AdminReportService.ACTION_TAKEDOWN, 99L);

    assertFalse(share.isActive());
    assertEquals(ContentReport.RESOLVED, report.getStatus());
  }

  @Test
  void 기각하면_콘텐츠는_그대로_두고_신고만_닫는다() {
    ContentReport report = new ContentReport(1L, ContentReport.TARGET_SHARE, 5L, "SPAM", null);
    when(reportRepository.findById(10L)).thenReturn(Optional.of(report));

    service.resolve(10L, AdminReportService.ACTION_DISMISS, 99L);

    assertEquals(ContentReport.DISMISSED, report.getStatus());
  }

  @Test
  void 알_수_없는_처리_방식과_없는_신고는_거부한다() {
    ContentReport report = new ContentReport(1L, ContentReport.TARGET_SHARE, 5L, "SPAM", null);
    when(reportRepository.findById(10L)).thenReturn(Optional.of(report));

    assertThrows(JournalShareException.class, () -> service.resolve(10L, "BAN", 99L));
    assertThrows(JournalShareException.class, () -> service.resolve(11L, "DISMISS", 99L));
  }
}
