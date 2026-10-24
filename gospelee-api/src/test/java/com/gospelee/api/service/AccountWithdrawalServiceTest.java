package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountBibleReadingGoal;
import com.gospelee.api.entity.AccountBibleReadingGoalMember;
import com.gospelee.api.entity.AppleAuthToken;
import com.gospelee.api.entity.Journal;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.exception.AccountNotFoundException;
import com.gospelee.api.repository.jpa.account.AccountBibleReadRepository;
import com.gospelee.api.repository.jpa.account.AccountBibleReadingGoalMemberRepository;
import com.gospelee.api.repository.jpa.account.AccountBibleReadingGoalRepository;
import com.gospelee.api.repository.jpa.account.AccountBibleWriteRepository;
import com.gospelee.api.repository.jpa.account.AccountBlockRepository;
import com.gospelee.api.repository.jpa.account.AccountMetaRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.account.AppleAuthTokenRepository;
import com.gospelee.api.repository.jpa.ecclesia.AccountEcclesiaHistoryJpaRepository;
import com.gospelee.api.repository.jpa.journal.JournalRepository;
import com.gospelee.api.repository.jpa.journal.JournalShareReceivedRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.repository.jpa.qrlogin.QrloginRepository;
import com.gospelee.api.service.reaction.ReactionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountWithdrawalServiceTest {

  private static final long UID = 7L;

  private AccountRepository accountRepository;
  private AccountMetaRepository accountMetaRepository;
  private AppleAuthTokenRepository appleAuthTokenRepository;
  private AppleAuthClient appleAuthClient;
  private BibleReadingService bibleReadingService;
  private AccountBibleReadingGoalRepository goalRepository;
  private AccountBibleReadingGoalMemberRepository goalMemberRepository;
  private AccountBibleReadRepository bibleReadRepository;
  private AccountBibleWriteRepository bibleWriteRepository;
  private AccountBlockRepository blockRepository;
  private AccountEcclesiaHistoryJpaRepository ecclesiaHistoryRepository;
  private JournalRepository journalRepository;
  private JournalShareReceivedRepository shareReceivedRepository;
  private PushNotificationReceiversRepository pushReceiversRepository;
  private QrloginRepository qrloginRepository;
  private ReactionService reactionService;
  private AccountWithdrawalService service;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    accountMetaRepository = mock(AccountMetaRepository.class);
    appleAuthTokenRepository = mock(AppleAuthTokenRepository.class);
    appleAuthClient = mock(AppleAuthClient.class);
    bibleReadingService = mock(BibleReadingService.class);
    goalRepository = mock(AccountBibleReadingGoalRepository.class);
    goalMemberRepository = mock(AccountBibleReadingGoalMemberRepository.class);
    bibleReadRepository = mock(AccountBibleReadRepository.class);
    bibleWriteRepository = mock(AccountBibleWriteRepository.class);
    blockRepository = mock(AccountBlockRepository.class);
    ecclesiaHistoryRepository = mock(AccountEcclesiaHistoryJpaRepository.class);
    journalRepository = mock(JournalRepository.class);
    shareReceivedRepository = mock(JournalShareReceivedRepository.class);
    pushReceiversRepository = mock(PushNotificationReceiversRepository.class);
    qrloginRepository = mock(QrloginRepository.class);
    reactionService = mock(ReactionService.class);
    service = new AccountWithdrawalService(accountRepository, accountMetaRepository,
        appleAuthTokenRepository, appleAuthClient, bibleReadingService, goalRepository,
        goalMemberRepository, bibleReadRepository, bibleWriteRepository, blockRepository,
        ecclesiaHistoryRepository, journalRepository, shareReceivedRepository,
        pushReceiversRepository, qrloginRepository, reactionService);

    when(goalMemberRepository.findAllByAccountUid(anyLong())).thenReturn(List.of());
    when(goalRepository.findAllByAccountUidOrderByIdxDesc(anyLong())).thenReturn(List.of());
    when(journalRepository.findByAccountUidWithJournalBibles(anyLong())).thenReturn(List.of());
    when(appleAuthTokenRepository.findById(anyLong())).thenReturn(Optional.empty());
  }

  private Account fullAccount() {
    return Account.builder().uid(UID).name("홍길동").nickname("길동").ecclesiaUid(3L)
        .rrn("900101-1").phone("01012345678").email("a@b.c").role(RoleType.ADMIN)
        .idToken("id-token").pushToken("push").leaveYn(Yn.N).build();
  }

  @Test
  void 탈퇴하면_개인정보를_모두_비우고_탈퇴_처리한다() {
    Account account = fullAccount();
    when(accountRepository.findById(UID)).thenReturn(Optional.of(account));

    var result = service.withdraw(UID);

    assertEquals(UID, result.getUid());
    assertNull(account.getName());
    assertNull(account.getNickname());
    assertNull(account.getEcclesiaUid());
    assertNull(account.getRrn());
    assertNull(account.getPhone());
    assertNull(account.getEmail());
    assertNull(account.getIdToken());
    assertNull(account.getPushToken());
    assertEquals(RoleType.LAYMAN, account.getRole());
    assertEquals(Yn.Y, account.getLeaveYn());
    verify(accountRepository).save(account);
  }

  @Test
  void 탈퇴하면_개인_기록을_삭제한다() {
    Account account = fullAccount();
    when(accountRepository.findById(UID)).thenReturn(Optional.of(account));
    Journal journal = Journal.builder().uid(1L).accountUid(UID).content("묵상").build();
    when(journalRepository.findByAccountUidWithJournalBibles(UID)).thenReturn(List.of(journal));

    service.withdraw(UID);

    verify(bibleReadRepository).deleteAllByAccountUid(UID);
    verify(bibleWriteRepository).deleteByAccountUid(UID);
    verify(goalMemberRepository).deleteByAccountUid(UID);
    verify(blockRepository).deleteByBlockerUid(UID);
    verify(blockRepository).deleteByBlockedUid(UID);
    verify(ecclesiaHistoryRepository).deleteByAccountUid(UID);
    verify(shareReceivedRepository).deleteByAccountUid(UID);
    verify(pushReceiversRepository).deleteByReceiveAccountUid(UID);
    verify(accountMetaRepository).deleteByAccountUid(UID);
    verify(qrloginRepository).deleteByEmail("a@b.c");
    verify(journalRepository).deleteAll(List.of(journal));
  }

  @Test
  void 이메일이_없으면_QR로그인_삭제를_건너뛴다() {
    Account account = Account.builder().uid(UID).leaveYn(Yn.Y).build();
    when(accountRepository.findById(UID)).thenReturn(Optional.of(account));

    service.withdraw(UID);

    verify(qrloginRepository, never()).deleteByEmail(any());
  }

  @Test
  void 애플_토큰은_폐기하고_삭제한다() {
    Account account = fullAccount();
    when(accountRepository.findById(UID)).thenReturn(Optional.of(account));
    AppleAuthToken token = new AppleAuthToken(UID, "refresh");
    when(appleAuthTokenRepository.findById(UID)).thenReturn(Optional.of(token));
    when(appleAuthClient.revoke("refresh")).thenReturn(true);

    service.withdraw(UID);

    verify(appleAuthClient).revoke("refresh");
    verify(appleAuthTokenRepository).delete(token);
  }

  @Test
  void 애플_토큰_폐기에_실패해도_탈퇴는_진행된다() {
    Account account = fullAccount();
    when(accountRepository.findById(UID)).thenReturn(Optional.of(account));
    AppleAuthToken token = new AppleAuthToken(UID, "refresh");
    when(appleAuthTokenRepository.findById(UID)).thenReturn(Optional.of(token));
    when(appleAuthClient.revoke("refresh")).thenReturn(false);

    service.withdraw(UID);

    verify(appleAuthTokenRepository).delete(token);
    assertEquals(Yn.Y, account.getLeaveYn());
  }

  @Test
  void 참여중이거나_방장인_진행중_통독_목표에서_나간다() {
    Account account = fullAccount();
    when(accountRepository.findById(UID)).thenReturn(Optional.of(account));

    when(goalMemberRepository.findAllByAccountUid(UID)).thenReturn(List.of(
        member(10L, "JOINED"), member(11L, "LEFT")));
    when(goalRepository.findAllByAccountUidOrderByIdxDesc(UID)).thenReturn(List.of(
        goal(20L, "PROGRESS"), goal(21L, "CANCELED"), goal(10L, "PROGRESS")));
    when(goalRepository.existsById(anyLong())).thenReturn(true);

    service.withdraw(UID);

    verify(bibleReadingService).leaveGoal(UID, 10L);
    verify(bibleReadingService).leaveGoal(UID, 20L);
    verify(bibleReadingService, never()).leaveGoal(UID, 11L);
    verify(bibleReadingService, never()).leaveGoal(UID, 21L);
  }

  @Test
  void 없는_목표는_건너뛴다() {
    Account account = fullAccount();
    when(accountRepository.findById(UID)).thenReturn(Optional.of(account));
    when(goalMemberRepository.findAllByAccountUid(UID)).thenReturn(List.of(member(10L, "JOINED")));
    when(goalRepository.existsById(10L)).thenReturn(false);

    service.withdraw(UID);

    verify(bibleReadingService, never()).leaveGoal(anyLong(), anyLong());
  }

  @Test
  void 계정이_없으면_예외() {
    when(accountRepository.findById(UID)).thenReturn(Optional.empty());

    assertThrows(AccountNotFoundException.class, () -> service.withdraw(UID));
  }

  private static AccountBibleReadingGoalMember member(long goalIdx, String status) {
    return AccountBibleReadingGoalMember.builder().goalIdx(goalIdx).accountUid(UID)
        .role("MEMBER").status(status).build();
  }

  private static AccountBibleReadingGoal goal(long idx, String status) {
    return AccountBibleReadingGoal.builder().idx(idx).accountUid(UID).status(status).build();
  }
}
