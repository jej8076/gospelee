package com.gospelee.api.service;

import com.gospelee.api.dto.journalshare.JournalShareDTOs.BlockedUser;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.CommentItem;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.PublicShare;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.ShareResult;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.ShareView;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountBlock;
import com.gospelee.api.entity.Bible;
import com.gospelee.api.entity.ContentReport;
import com.gospelee.api.entity.Journal;
import com.gospelee.api.entity.JournalBible;
import com.gospelee.api.entity.JournalShare;
import com.gospelee.api.entity.JournalShareComment;
import com.gospelee.api.repository.jpa.account.AccountBlockRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.bible.BibleRepository;
import com.gospelee.api.repository.jpa.journal.ContentReportRepository;
import com.gospelee.api.repository.jpa.journal.JournalRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareCommentRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareRepository;
import com.gospelee.api.utils.VerseReferenceFormatter;
import com.gospelee.api.utils.VerseReferenceFormatter.VerseLine;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalShareService {

  public static final int COMMENT_MAX_LENGTH = 500;
  private static final int REPORT_DETAIL_MAX_LENGTH = 300;
  private static final Set<String> REPORT_REASONS = Set.of("SPAM", "ABUSE", "SEXUAL", "HERESY",
      "PRIVACY", "OTHER");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd HH:mm:ss");
  private static final SecureRandom RANDOM = new SecureRandom();

  private final JournalRepository journalRepository;
  private final JournalShareRepository shareRepository;
  private final JournalShareCommentRepository commentRepository;
  private final ContentReportRepository reportRepository;
  private final AccountBlockRepository blockRepository;
  private final AccountRepository accountRepository;
  private final BibleRepository bibleRepository;

  @Value("${journal-share.base-url:https://api.po-do.org/api}")
  private String baseUrl;

  // ========== 공유 ==========

  /** 묵상을 공유한다. 이미 공유한 묵상이면 같은 링크로 내용을 갱신한다 */
  @Transactional
  public ShareResult share(long accountUid, long journalUid) {
    String nickname = requireNickname(accountUid);
    Journal journal = journalRepository.findById(journalUid)
        .orElseThrow(() -> new JournalShareException(JournalShareException.NOT_FOUND,
            "묵상을 찾을 수 없습니다."));
    if (!journal.getAccountUid().equals(accountUid)) {
      throw new JournalShareException(JournalShareException.FORBIDDEN, "내 묵상만 공유할 수 있습니다.");
    }
    if (journal.getContent() == null || journal.getContent().isBlank()) {
      throw new JournalShareException(JournalShareException.INVALID, "내용이 있는 묵상만 공유할 수 있습니다.");
    }

    List<VerseLine> lines = loadVerseLines(journal.getJournalBibleList());
    String reference = VerseReferenceFormatter.reference(lines);
    String verseText = VerseReferenceFormatter.text(lines);

    JournalShare share = shareRepository.findByJournalUid(journalUid)
        .map(existing -> {
          existing.refresh(reference, verseText, journal.getContent());
          return existing;
        })
        .orElseGet(() -> JournalShare.builder()
            .journalUid(journalUid)
            .accountUid(accountUid)
            .token(newToken())
            .reference(reference)
            .verseText(verseText)
            .content(journal.getContent())
            .build());
    share = shareRepository.save(share);

    return new ShareResult(share.getToken(), shareUrl(share.getToken()), reference, nickname);
  }

  /** 공유를 취소한다. 링크는 즉시 열리지 않게 된다 */
  @Transactional
  public void revoke(long accountUid, long journalUid) {
    shareRepository.findByJournalUid(journalUid).ifPresent(share -> {
      if (!share.getAccountUid().equals(accountUid)) {
        throw new JournalShareException(JournalShareException.FORBIDDEN, "내 공유만 취소할 수 있습니다.");
      }
      share.revoke();
    });
  }

  /** 묵상 삭제 시 함께 공유를 닫는다 */
  @Transactional
  public void revokeByJournal(long journalUid) {
    shareRepository.findByJournalUid(journalUid).ifPresent(JournalShare::revoke);
  }

  /** 내 묵상 중 공유 중인 묵상의 uid -> token */
  @Transactional(readOnly = true)
  public Map<Long, String> activeTokensByJournal(List<Long> journalUids) {
    if (journalUids.isEmpty()) {
      return Map.of();
    }
    return shareRepository.findByJournalUidIn(journalUids).stream()
        .filter(JournalShare::isActive)
        .collect(Collectors.toMap(JournalShare::getJournalUid, JournalShare::getToken));
  }

  // ========== 조회 ==========

  /** 로그인한 사용자가 공유 상세와 댓글을 본다 */
  @Transactional(readOnly = true)
  public ShareView view(long viewerUid, String token) {
    JournalShare share = findActiveShare(token);
    Set<Long> blocked = blockedUids(viewerUid);
    if (blocked.contains(share.getAccountUid())) {
      throw new JournalShareException(JournalShareException.NOT_FOUND, "볼 수 없는 공유입니다.");
    }

    List<JournalShareComment> comments = commentRepository
        .findByShareUidAndStatusOrderByUidAsc(share.getUid(), JournalShareComment.ACTIVE).stream()
        .filter(c -> !blocked.contains(c.getAccountUid()))
        .toList();

    Map<Long, String> nicknames = nicknames(collectUids(share, comments));
    List<CommentItem> items = comments.stream()
        .map(c -> new CommentItem(c.getUid(), c.getAccountUid(),
            nicknames.getOrDefault(c.getAccountUid(), "성도"), c.getContent(),
            c.getAccountUid().equals(viewerUid), format(c)))
        .toList();

    return new ShareView(share.getToken(), share.getUid(), share.getReference(), share.getVerseText(),
        share.getContent(), share.getAccountUid(),
        nicknames.getOrDefault(share.getAccountUid(), "성도"),
        share.getAccountUid().equals(viewerUid), format(share), items);
  }

  /** 비회원이 웹 페이지로 보는 공유 내용. 없거나 취소된 공유면 null */
  @Transactional(readOnly = true)
  public PublicShare publicView(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    return shareRepository.findByToken(token)
        .filter(JournalShare::isActive)
        .map(share -> new PublicShare(share.getToken(), share.getReference(),
            share.getVerseText(), share.getContent(),
            nicknames(Set.of(share.getAccountUid())).getOrDefault(share.getAccountUid(), "성도"),
            commentRepository.countByShareUidAndStatus(share.getUid(),
                JournalShareComment.ACTIVE)))
        .orElse(null);
  }

  // ========== 댓글 ==========

  @Transactional
  public CommentItem addComment(long accountUid, String token, String content) {
    String nickname = requireNickname(accountUid);
    String text = content == null ? "" : content.trim();
    if (text.isEmpty() || text.length() > COMMENT_MAX_LENGTH) {
      throw new JournalShareException(JournalShareException.INVALID,
          "댓글은 1~" + COMMENT_MAX_LENGTH + "자로 입력해주세요.");
    }
    JournalShare share = findActiveShare(token);
    if (blockRepository.existsByBlockerUidAndBlockedUid(share.getAccountUid(), accountUid)) {
      throw new JournalShareException(JournalShareException.FORBIDDEN, "댓글을 작성할 수 없습니다.");
    }

    JournalShareComment saved = commentRepository.save(
        new JournalShareComment(share.getUid(), accountUid, text));
    return new CommentItem(saved.getUid(), accountUid, nickname, text, true, format(saved));
  }

  /** 댓글 작성자 또는 공유한 사람이 삭제할 수 있다 */
  @Transactional
  public void deleteComment(long accountUid, long commentUid) {
    JournalShareComment comment = commentRepository.findById(commentUid)
        .filter(JournalShareComment::isActive)
        .orElseThrow(() -> new JournalShareException(JournalShareException.NOT_FOUND,
            "댓글을 찾을 수 없습니다."));
    boolean author = comment.getAccountUid().equals(accountUid);
    boolean shareOwner = shareRepository.findById(comment.getShareUid())
        .map(share -> share.getAccountUid().equals(accountUid))
        .orElse(false);
    if (!author && !shareOwner) {
      throw new JournalShareException(JournalShareException.FORBIDDEN, "삭제 권한이 없습니다.");
    }
    comment.delete();
  }

  // ========== 신고 / 차단 ==========

  @Transactional
  public void report(long reporterUid, String targetType, Long targetUid, String reason,
      String detail) {
    if (targetUid == null || !(ContentReport.TARGET_SHARE.equals(targetType)
        || ContentReport.TARGET_COMMENT.equals(targetType))) {
      throw new JournalShareException(JournalShareException.INVALID, "신고 대상이 올바르지 않습니다.");
    }
    if (reason == null || !REPORT_REASONS.contains(reason)) {
      throw new JournalShareException(JournalShareException.INVALID, "신고 사유를 선택해주세요.");
    }
    boolean exists = ContentReport.TARGET_SHARE.equals(targetType)
        ? shareRepository.existsById(targetUid)
        : commentRepository.existsById(targetUid);
    if (!exists) {
      throw new JournalShareException(JournalShareException.NOT_FOUND, "신고 대상을 찾을 수 없습니다.");
    }
    if (reportRepository.existsByReporterUidAndTargetTypeAndTargetUid(reporterUid, targetType,
        targetUid)) {
      throw new JournalShareException(JournalShareException.DUPLICATE, "이미 신고한 항목입니다.");
    }

    String safeDetail = detail == null ? null : detail.trim();
    if (safeDetail != null && safeDetail.length() > REPORT_DETAIL_MAX_LENGTH) {
      safeDetail = safeDetail.substring(0, REPORT_DETAIL_MAX_LENGTH);
    }
    reportRepository.save(new ContentReport(reporterUid, targetType, targetUid, reason,
        safeDetail));
    log.warn("[REPORT    ] reporter:{} type:{} target:{} reason:{}", reporterUid, targetType,
        targetUid, reason);
  }

  @Transactional
  public void block(long blockerUid, Long blockedUid) {
    if (blockedUid == null || blockedUid == blockerUid) {
      throw new JournalShareException(JournalShareException.INVALID, "차단할 수 없는 대상입니다.");
    }
    if (!accountRepository.existsById(blockedUid)) {
      throw new JournalShareException(JournalShareException.NOT_FOUND, "사용자를 찾을 수 없습니다.");
    }
    if (!blockRepository.existsByBlockerUidAndBlockedUid(blockerUid, blockedUid)) {
      blockRepository.save(new AccountBlock(blockerUid, blockedUid));
    }
  }

  @Transactional
  public void unblock(long blockerUid, long blockedUid) {
    blockRepository.findByBlockerUidAndBlockedUid(blockerUid, blockedUid)
        .ifPresent(blockRepository::delete);
  }

  @Transactional(readOnly = true)
  public List<BlockedUser> blockedUsers(long blockerUid) {
    List<Long> uids = blockRepository.findByBlockerUid(blockerUid).stream()
        .map(AccountBlock::getBlockedUid).toList();
    Map<Long, String> names = nicknames(new HashSet<>(uids));
    return uids.stream()
        .map(uid -> new BlockedUser(uid, names.getOrDefault(uid, "성도")))
        .toList();
  }

  // ========== 내부 ==========

  private String requireNickname(long accountUid) {
    Account account = accountRepository.findById(accountUid)
        .orElseThrow(() -> new JournalShareException(JournalShareException.NOT_FOUND,
            "계정을 찾을 수 없습니다."));
    String nickname = account.getNickname();
    if (nickname == null || nickname.isBlank()) {
      throw new JournalShareException(JournalShareException.NICKNAME_REQUIRED,
          "닉네임을 먼저 설정해주세요.");
    }
    return nickname;
  }

  private JournalShare findActiveShare(String token) {
    return shareRepository.findByToken(token)
        .filter(JournalShare::isActive)
        .orElseThrow(() -> new JournalShareException(JournalShareException.NOT_FOUND,
            "삭제되었거나 공유가 취소된 묵상입니다."));
  }

  private Set<Long> blockedUids(long viewerUid) {
    return blockRepository.findByBlockerUid(viewerUid).stream()
        .map(AccountBlock::getBlockedUid)
        .collect(Collectors.toSet());
  }

  private Set<Long> collectUids(JournalShare share, List<JournalShareComment> comments) {
    Set<Long> uids = new HashSet<>();
    uids.add(share.getAccountUid());
    comments.forEach(c -> uids.add(c.getAccountUid()));
    return uids;
  }

  /** 탈퇴 등으로 닉네임이 없는 계정은 맵에서 빠진다 */
  private Map<Long, String> nicknames(Set<Long> uids) {
    Map<Long, String> result = new HashMap<>();
    accountRepository.findAllById(uids).forEach(account -> {
      if (account.getNickname() != null && !account.getNickname().isBlank()) {
        result.put(account.getUid(), account.getNickname());
      }
    });
    return result;
  }

  private List<VerseLine> loadVerseLines(List<JournalBible> journalBibles) {
    if (journalBibles == null || journalBibles.isEmpty()) {
      return List.of();
    }
    Map<String, TreeSet<Integer>> wanted = new HashMap<>();
    for (JournalBible jb : journalBibles) {
      wanted.computeIfAbsent(jb.getBook() + ":" + jb.getChapter(), k -> new TreeSet<>())
          .add(jb.getVerse());
    }

    List<VerseLine> lines = new ArrayList<>();
    wanted.forEach((key, verses) -> {
      String[] parts = key.split(":");
      int book = Integer.parseInt(parts[0]);
      int chapter = Integer.parseInt(parts[1]);
      List<Bible> chapterVerses = bibleRepository
          .findByBookAndChapterOrderByIdxAsc(book, chapter).orElse(List.of());
      for (Bible bible : chapterVerses) {
        if (verses.contains(bible.getVerse())) {
          lines.add(new VerseLine(book, bible.getLongLabel(), chapter, bible.getVerse(),
              bible.getSentence()));
        }
      }
    });
    return lines;
  }

  private String shareUrl(String token) {
    return baseUrl + "/journal-share/" + token;
  }

  private static String newToken() {
    byte[] bytes = new byte[16];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String format(com.gospelee.api.entity.common.EditInfomation entity) {
    return entity.getInsertTime() == null ? null : entity.getInsertTime().format(TIME_FORMAT);
  }
}
