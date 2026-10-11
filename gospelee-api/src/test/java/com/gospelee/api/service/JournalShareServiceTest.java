package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.journalshare.JournalShareDTOs.ShareResult;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.ShareView;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountBlock;
import com.gospelee.api.entity.Bible;
import com.gospelee.api.entity.Journal;
import com.gospelee.api.entity.JournalBible;
import com.gospelee.api.entity.JournalShare;
import com.gospelee.api.entity.JournalShareComment;
import com.gospelee.api.entity.JournalShareReceived;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.ShareListItem;
import com.gospelee.api.repository.jpa.account.AccountBlockRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.bible.BibleRepository;
import com.gospelee.api.repository.jpa.journal.ContentReportRepository;
import com.gospelee.api.repository.jpa.journal.JournalRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareCommentRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareReceivedRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JournalShareServiceTest {

  private JournalRepository journalRepository;
  private JournalShareRepository shareRepository;
  private JournalShareCommentRepository commentRepository;
  private ContentReportRepository reportRepository;
  private AccountBlockRepository blockRepository;
  private AccountRepository accountRepository;
  private BibleRepository bibleRepository;
  private JournalShareReceivedRepository receivedRepository;
  private JournalShareService service;

  @BeforeEach
  void setUp() throws Exception {
    journalRepository = mock(JournalRepository.class);
    shareRepository = mock(JournalShareRepository.class);
    commentRepository = mock(JournalShareCommentRepository.class);
    reportRepository = mock(ContentReportRepository.class);
    blockRepository = mock(AccountBlockRepository.class);
    accountRepository = mock(AccountRepository.class);
    bibleRepository = mock(BibleRepository.class);
    receivedRepository = mock(JournalShareReceivedRepository.class);
    service = new JournalShareService(journalRepository, shareRepository, commentRepository,
        reportRepository, blockRepository, accountRepository, bibleRepository,
        receivedRepository);
    set(service, "baseUrl", "https://example.test/api");
    when(shareRepository.save(any(JournalShare.class))).thenAnswer(i -> i.getArgument(0));
  }

  private static void set(Object target, String field, Object value) throws Exception {
    Field f = target.getClass().getDeclaredField(field);
    f.setAccessible(true);
    f.set(target, value);
  }

  private Account account(long uid, String nickname) {
    return Account.builder().uid(uid).nickname(nickname).build();
  }

  private Journal journal(long uid, long owner, String content) {
    Journal journal = Journal.builder().uid(uid).accountUid(owner).content(content).build();
    journal.changeJournalBibleList(List.of(
        JournalBible.builder().book(43).chapter(3).verse(16).build(),
        JournalBible.builder().book(43).chapter(3).verse(17).build()));
    return journal;
  }

  private void stubBible() {
    Bible v16 = mock(Bible.class);
    when(v16.getVerse()).thenReturn(16);
    when(v16.getLongLabel()).thenReturn("요한복음");
    when(v16.getSentence()).thenReturn("하나님이 세상을");
    Bible v17 = mock(Bible.class);
    when(v17.getVerse()).thenReturn(17);
    when(v17.getLongLabel()).thenReturn("요한복음");
    when(v17.getSentence()).thenReturn("아들을 보내신");
    Bible v18 = mock(Bible.class);
    when(v18.getVerse()).thenReturn(18);
    when(bibleRepository.findByBookAndChapterOrderByIdxAsc(43, 3))
        .thenReturn(Optional.of(List.of(v16, v17, v18)));
  }

  @Test
  void 닉네임이_없으면_공유할_수_없다() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1, null)));

    JournalShareException e = assertThrows(JournalShareException.class,
        () -> service.share(1, 10));

    assertEquals(JournalShareException.NICKNAME_REQUIRED, e.getCode());
    verify(shareRepository, never()).save(any());
  }

  @Test
  void 남의_묵상은_공유할_수_없다() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1, "포도")));
    when(journalRepository.findById(10L)).thenReturn(Optional.of(journal(10, 2, "내용")));

    JournalShareException e = assertThrows(JournalShareException.class,
        () -> service.share(1, 10));

    assertEquals(JournalShareException.FORBIDDEN, e.getCode());
  }

  @Test
  void 공유하면_구절_스냅샷과_링크가_만들어진다() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1, "포도")));
    when(journalRepository.findById(10L)).thenReturn(Optional.of(journal(10, 1, "은혜로운 묵상")));
    when(shareRepository.findByJournalUid(10L)).thenReturn(Optional.empty());
    stubBible();

    ShareResult result = service.share(1, 10);

    assertEquals("요한복음 3:16-17", result.reference());
    assertTrue(result.url().startsWith("https://example.test/api/journal-share/"));
    assertTrue(result.url().endsWith(result.token()));
    assertEquals(22, result.token().length());
  }

  @Test
  void 이미_공유한_묵상은_같은_링크로_갱신한다() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1, "포도")));
    when(journalRepository.findById(10L)).thenReturn(Optional.of(journal(10, 1, "수정한 묵상")));
    JournalShare existing = JournalShare.builder().journalUid(10L).accountUid(1L).token("same")
        .content("옛 묵상").build();
    existing.revoke();
    when(shareRepository.findByJournalUid(10L)).thenReturn(Optional.of(existing));
    stubBible();

    ShareResult result = service.share(1, 10);

    assertEquals("same", result.token());
    assertTrue(existing.isActive());
    assertEquals("수정한 묵상", existing.getContent());
  }

  @Test
  void 취소된_공유는_볼_수_없다() {
    JournalShare share = JournalShare.builder().journalUid(10L).accountUid(1L).token("t").build();
    share.revoke();
    when(shareRepository.findByToken("t")).thenReturn(Optional.of(share));

    assertThrows(JournalShareException.class, () -> service.view(5, "t"));
    assertEquals(null, service.publicView("t"));
  }

  @Test
  void 내가_차단한_사용자의_공유는_볼_수_없다() {
    JournalShare share = JournalShare.builder().journalUid(10L).accountUid(1L).token("t").build();
    when(shareRepository.findByToken("t")).thenReturn(Optional.of(share));
    when(blockRepository.findByBlockerUid(5L)).thenReturn(List.of(new AccountBlock(5L, 1L)));

    JournalShareException e = assertThrows(JournalShareException.class,
        () -> service.view(5, "t"));

    assertEquals(JournalShareException.NOT_FOUND, e.getCode());
  }

  @Test
  void 차단한_사용자의_댓글은_목록에서_빠진다() {
    JournalShare share = Mockito.spy(
        JournalShare.builder().journalUid(10L).accountUid(1L).token("t").build());
    when(share.getUid()).thenReturn(100L);
    when(shareRepository.findByToken("t")).thenReturn(Optional.of(share));
    when(blockRepository.findByBlockerUid(5L)).thenReturn(List.of(new AccountBlock(5L, 7L)));
    JournalShareComment fromBlocked = new JournalShareComment(100L, 7L, "차단된 댓글");
    JournalShareComment fromOther = new JournalShareComment(100L, 8L, "정상 댓글");
    when(commentRepository.findByShareUidAndStatusOrderByUidAsc(100L, "ACTIVE"))
        .thenReturn(List.of(fromBlocked, fromOther));
    when(accountRepository.findAllById(any())).thenReturn(
        List.of(account(1, "작성자"), account(8, "이웃")));

    ShareView view = service.view(5, "t");

    assertEquals(1, view.comments().size());
    assertEquals("정상 댓글", view.comments().get(0).content());
    assertEquals("이웃", view.comments().get(0).nickname());
    assertFalse(view.mine());
  }

  @Test
  void 닉네임이_없으면_댓글을_쓸_수_없다() {
    when(accountRepository.findById(5L)).thenReturn(Optional.of(account(5, "  ")));

    JournalShareException e = assertThrows(JournalShareException.class,
        () -> service.addComment(5, "t", "아멘"));

    assertEquals(JournalShareException.NICKNAME_REQUIRED, e.getCode());
  }

  @Test
  void 빈_댓글과_너무_긴_댓글은_거부() {
    when(accountRepository.findById(5L)).thenReturn(Optional.of(account(5, "이웃")));

    assertThrows(JournalShareException.class, () -> service.addComment(5, "t", "   "));
    assertThrows(JournalShareException.class,
        () -> service.addComment(5, "t", "가".repeat(JournalShareService.COMMENT_MAX_LENGTH + 1)));
  }

  @Test
  void 공유자에게_차단당한_사용자는_댓글을_쓸_수_없다() {
    when(accountRepository.findById(5L)).thenReturn(Optional.of(account(5, "이웃")));
    JournalShare share = JournalShare.builder().journalUid(10L).accountUid(1L).token("t").build();
    when(shareRepository.findByToken("t")).thenReturn(Optional.of(share));
    when(blockRepository.existsByBlockerUidAndBlockedUid(1L, 5L)).thenReturn(true);

    JournalShareException e = assertThrows(JournalShareException.class,
        () -> service.addComment(5, "t", "아멘"));

    assertEquals(JournalShareException.FORBIDDEN, e.getCode());
    verify(commentRepository, never()).save(any());
  }

  @Test
  void 댓글은_작성자와_공유자만_삭제할_수_있다() {
    JournalShareComment comment = new JournalShareComment(100L, 8L, "댓글");
    when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));
    JournalShare share = JournalShare.builder().journalUid(10L).accountUid(1L).token("t").build();
    when(shareRepository.findById(100L)).thenReturn(Optional.of(share));

    assertThrows(JournalShareException.class, () -> service.deleteComment(99, 50));
    assertTrue(comment.isActive());

    service.deleteComment(1, 50); // 공유한 사람
    assertFalse(comment.isActive());
  }

  @Test
  void 같은_대상을_중복_신고할_수_없다() {
    when(shareRepository.existsById(100L)).thenReturn(true);
    when(reportRepository.existsByReporterUidAndTargetTypeAndTargetUid(5L, "SHARE", 100L))
        .thenReturn(true);

    JournalShareException e = assertThrows(JournalShareException.class,
        () -> service.report(5, "SHARE", 100L, "SPAM", null));

    assertEquals(JournalShareException.DUPLICATE, e.getCode());
  }

  @Test
  void 올바르지_않은_신고_사유는_거부() {
    assertThrows(JournalShareException.class,
        () -> service.report(5, "SHARE", 100L, "WHATEVER", null));
    assertThrows(JournalShareException.class,
        () -> service.report(5, "UNKNOWN", 100L, "SPAM", null));
  }

  @Test
  void 자기_자신은_차단할_수_없다() {
    assertThrows(JournalShareException.class, () -> service.block(5, 5L));
  }

  private JournalShare shareWithUid(long uid, long owner, String token, String content)
      throws Exception {
    JournalShare share = JournalShare.builder().journalUid(uid * 10).accountUid(owner)
        .token(token).reference("요 3:16").content(content).build();
    set(share, "uid", uid);
    return share;
  }

  @Test
  void 남의_공유를_열면_받은_묵상으로_기록하고_내_공유는_기록하지_않는다() throws Exception {
    JournalShare share = shareWithUid(3, 1, "t", "묵상");
    when(shareRepository.findByToken("t")).thenReturn(Optional.of(share));
    when(receivedRepository.findByAccountUidAndShareUid(5L, 3L)).thenReturn(Optional.empty());

    service.view(5, "t");
    service.view(1, "t");

    verify(receivedRepository, Mockito.times(1)).save(any(JournalShareReceived.class));
  }

  @Test
  void 이미_받은_묵상을_다시_열면_새로_저장하지_않고_시각만_갱신한다() throws Exception {
    JournalShare share = shareWithUid(3, 1, "t", "묵상");
    JournalShareReceived existing = new JournalShareReceived(5L, 3L);
    when(shareRepository.findByToken("t")).thenReturn(Optional.of(share));
    when(receivedRepository.findByAccountUidAndShareUid(5L, 3L))
        .thenReturn(Optional.of(existing));

    service.view(5, "t");

    verify(receivedRepository, never()).save(any(JournalShareReceived.class));
  }

  @Test
  void 받은_묵상_목록은_최근_본_순이고_취소된_공유와_차단한_작성자는_뺀다() throws Exception {
    JournalShare a = shareWithUid(1, 11, "a", "A 묵상");
    JournalShare revoked = shareWithUid(2, 12, "b", "B 묵상");
    revoked.revoke();
    JournalShare blockedAuthor = shareWithUid(3, 13, "c", "C 묵상");
    JournalShare d = shareWithUid(4, 14, "d", "D 묵상");
    when(receivedRepository.findByAccountUidOrderByLastViewedAtDesc(5L)).thenReturn(List.of(
        new JournalShareReceived(5L, 4L), new JournalShareReceived(5L, 3L),
        new JournalShareReceived(5L, 2L), new JournalShareReceived(5L, 1L)));
    when(shareRepository.findAllById(any())).thenReturn(List.of(a, revoked, blockedAuthor, d));
    when(blockRepository.findByBlockerUid(5L)).thenReturn(List.of(new AccountBlock(5L, 13L)));
    when(accountRepository.findAllById(any())).thenReturn(
        List.of(account(11, "가"), account(14, "라")));

    List<ShareListItem> list = service.receivedList(5);

    assertEquals(List.of("d", "a"), list.stream().map(ShareListItem::token).toList());
    assertEquals("라", list.get(0).authorNickname());
  }

  @Test
  void 내가_공유한_목록은_댓글_수와_내용_미리보기를_담는다() throws Exception {
    JournalShare share = shareWithUid(3, 1, "t", "긴 묵상 ".repeat(40));
    when(shareRepository.findByAccountUidAndStatusOrderByUidDesc(1L, JournalShare.ACTIVE))
        .thenReturn(List.of(share));
    when(commentRepository.countByShareUidAndStatus(3L, JournalShareComment.ACTIVE))
        .thenReturn(2L);
    when(accountRepository.findAllById(any())).thenReturn(List.of(account(1, "포도")));

    List<ShareListItem> list = service.mySharedList(1);

    assertEquals(1, list.size());
    assertEquals(2L, list.get(0).commentCount());
    assertEquals("포도", list.get(0).authorNickname());
    assertTrue(list.get(0).preview().endsWith("…"));
  }
}
