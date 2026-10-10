package com.gospelee.api.service;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.ContentReport;
import com.gospelee.api.entity.JournalShare;
import com.gospelee.api.entity.JournalShareComment;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.journal.ContentReportRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareCommentRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자용 신고 검토: 목록 조회, 콘텐츠 내리기, 기각 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReportService {

  public static final String ACTION_TAKEDOWN = "TAKEDOWN";
  public static final String ACTION_DISMISS = "DISMISS";

  private static final Set<String> STATUSES = Set.of(ContentReport.PENDING,
      ContentReport.RESOLVED, ContentReport.DISMISSED);

  private final ContentReportRepository reportRepository;
  private final JournalShareRepository shareRepository;
  private final JournalShareCommentRepository commentRepository;
  private final AccountRepository accountRepository;

  public record ReportItem(Long uid, String targetType, Long targetUid, String reason,
                           String detail, String status, String reporterNickname,
                           String authorNickname, Long authorUid, String reference,
                           String content, boolean targetActive, LocalDateTime reportedAt) {

  }

  @Transactional(readOnly = true)
  public List<ReportItem> list(String status) {
    String target = status == null || !STATUSES.contains(status) ? ContentReport.PENDING : status;
    List<ContentReport> reports = reportRepository.findByStatusOrderByUidDesc(target);

    Set<Long> shareUids = new HashSet<>();
    Set<Long> commentUids = new HashSet<>();
    for (ContentReport r : reports) {
      (ContentReport.TARGET_SHARE.equals(r.getTargetType()) ? shareUids : commentUids)
          .add(r.getTargetUid());
    }
    Map<Long, JournalShare> shares = shareRepository.findAllById(shareUids).stream()
        .collect(Collectors.toMap(JournalShare::getUid, s -> s));
    Map<Long, JournalShareComment> comments = commentRepository.findAllById(commentUids).stream()
        .collect(Collectors.toMap(JournalShareComment::getUid, c -> c));

    Set<Long> accountUids = new HashSet<>();
    reports.forEach(r -> accountUids.add(r.getReporterUid()));
    shares.values().forEach(s -> accountUids.add(s.getAccountUid()));
    comments.values().forEach(c -> accountUids.add(c.getAccountUid()));
    Map<Long, String> names = accountRepository.findAllById(accountUids).stream()
        .collect(Collectors.toMap(Account::getUid, a -> displayName(a)));

    return reports.stream().map(r -> {
      boolean isShare = ContentReport.TARGET_SHARE.equals(r.getTargetType());
      JournalShare share = isShare ? shares.get(r.getTargetUid()) : null;
      JournalShareComment comment = isShare ? null : comments.get(r.getTargetUid());
      Long authorUid = share != null ? share.getAccountUid()
          : comment != null ? comment.getAccountUid() : null;
      return new ReportItem(r.getUid(), r.getTargetType(), r.getTargetUid(), r.getReason(),
          r.getDetail(), r.getStatus(), names.getOrDefault(r.getReporterUid(), "-"),
          authorUid == null ? "-" : names.getOrDefault(authorUid, "-"), authorUid,
          share != null ? share.getReference() : null,
          share != null ? share.getContent() : comment != null ? comment.getContent() : null,
          share != null ? share.isActive() : comment != null && comment.isActive(),
          r.getInsertTime());
    }).toList();
  }

  /** TAKEDOWN: 대상을 내리고 같은 대상의 대기 신고를 모두 처리 / DISMISS: 이 신고만 기각 */
  @Transactional
  public void resolve(long reportUid, String action, long adminUid) {
    ContentReport report = reportRepository.findById(reportUid)
        .orElseThrow(() -> new JournalShareException(JournalShareException.NOT_FOUND,
            "신고를 찾을 수 없습니다."));
    if (ACTION_DISMISS.equals(action)) {
      report.close(ContentReport.DISMISSED);
    } else if (ACTION_TAKEDOWN.equals(action)) {
      if (ContentReport.TARGET_SHARE.equals(report.getTargetType())) {
        shareRepository.findById(report.getTargetUid()).ifPresent(JournalShare::revoke);
      } else {
        commentRepository.findById(report.getTargetUid()).ifPresent(JournalShareComment::delete);
      }
      reportRepository.findByTargetTypeAndTargetUidAndStatus(report.getTargetType(),
          report.getTargetUid(), ContentReport.PENDING)
          .forEach(r -> r.close(ContentReport.RESOLVED));
      report.close(ContentReport.RESOLVED);
    } else {
      throw new JournalShareException(JournalShareException.INVALID, "처리 방식이 올바르지 않습니다.");
    }
    log.warn("[REPORT    ] admin:{} report:{} action:{}", adminUid, reportUid, action);
  }

  private static String displayName(Account a) {
    String nick = a.getNickname();
    return nick != null && !nick.isBlank() ? nick : String.valueOf(a.getName());
  }
}
