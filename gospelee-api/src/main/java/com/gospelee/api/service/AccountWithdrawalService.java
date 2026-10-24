package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountLeaveResponseDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountBibleReadingGoal;
import com.gospelee.api.entity.AccountBibleReadingGoalMember;
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
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 회원 탈퇴 처리.
 * <ul>
 *   <li>개인정보(이름·닉네임·전화번호·이메일·토큰 등)와 개인 기록(묵상 일지, 성경 읽기/필기, 받은 목록,
 *       차단, 푸시 수신, 기도할게요 등 반응, 교회 가입 이력 등)은 삭제한다.</li>
 *   <li>다른 사용자에게 이미 공유된 나눔 묵상과 댓글은 남기되, 작성자는 닉네임이 비워진 탈퇴 계정
 *       (uid 만 존재)이라 "탈퇴한 사용자"로 익명 표시된다.</li>
 *   <li>신고 내역은 운영(검토)을 위해 남긴다.</li>
 * </ul>
 * 같은 소셜 계정으로 다시 로그인하면 이메일이 비워져 있으므로 새 계정으로 가입된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountWithdrawalService {

  private static final String GOAL_IN_PROGRESS = "PROGRESS";
  private static final String MEMBER_JOINED = "JOINED";

  private final AccountRepository accountRepository;
  private final AccountMetaRepository accountMetaRepository;
  private final AppleAuthTokenRepository appleAuthTokenRepository;
  private final AppleAuthClient appleAuthClient;
  private final BibleReadingService bibleReadingService;
  private final AccountBibleReadingGoalRepository goalRepository;
  private final AccountBibleReadingGoalMemberRepository goalMemberRepository;
  private final AccountBibleReadRepository bibleReadRepository;
  private final AccountBibleWriteRepository bibleWriteRepository;
  private final AccountBlockRepository blockRepository;
  private final AccountEcclesiaHistoryJpaRepository ecclesiaHistoryRepository;
  private final JournalRepository journalRepository;
  private final JournalShareReceivedRepository shareReceivedRepository;
  private final PushNotificationReceiversRepository pushReceiversRepository;
  private final QrloginRepository qrloginRepository;
  private final ReactionService reactionService;

  @Transactional
  public AccountLeaveResponseDTO withdraw(long accountUid) {
    Account account = accountRepository.findById(accountUid)
        .orElseThrow(() -> new AccountNotFoundException("탈퇴할 계정이 존재하지 않습니다."));

    // 이미 탈퇴한 계정이어도 다시 호출되면 남은 데이터를 한 번 더 정리한다 (멱등)
    String email = account.getEmail();

    revokeAppleLogin(accountUid);
    leaveReadingGoals(accountUid);

    bibleReadRepository.deleteAllByAccountUid(accountUid);
    bibleWriteRepository.deleteByAccountUid(accountUid);
    goalMemberRepository.deleteByAccountUid(accountUid);
    blockRepository.deleteByBlockerUid(accountUid);
    blockRepository.deleteByBlockedUid(accountUid);
    ecclesiaHistoryRepository.deleteByAccountUid(accountUid);
    shareReceivedRepository.deleteByAccountUid(accountUid);
    pushReceiversRepository.deleteByReceiveAccountUid(accountUid);
    reactionService.removeAllByAccount(accountUid);
    accountMetaRepository.deleteByAccountUid(accountUid);
    if (StringUtils.hasText(email)) {
      qrloginRepository.deleteByEmail(email);
    }

    // 개인 묵상 일지는 삭제한다. 공유된 묵상은 공유 시점의 내용이 별도로 저장돼 있어 그대로 남는다
    journalRepository.deleteAll(journalRepository.findByAccountUidWithJournalBibles(accountUid));

    account.withdraw();
    accountRepository.save(account);

    log.info("[ACCOUNT   ] withdraw uid:{}", accountUid);
    return AccountLeaveResponseDTO.builder().uid(accountUid).build();
  }

  /** 애플 로그인 연결 해제. 실패해도 탈퇴는 계속 진행한다 */
  private void revokeAppleLogin(long accountUid) {
    appleAuthTokenRepository.findById(accountUid).ifPresent(token -> {
      boolean revoked = appleAuthClient.revoke(token.getRefreshToken());
      log.info("[APPLE] 탈퇴 토큰 폐기 uid:{} revoked:{}", accountUid, revoked);
      appleAuthTokenRepository.delete(token);
    });
  }

  /** 참여 중이거나 방장인 통독 목표에서 나간다 (다른 참여자가 있으면 방장 위임, 없으면 목표 종료) */
  private void leaveReadingGoals(long accountUid) {
    Set<Long> goalIdxs = new LinkedHashSet<>();
    for (AccountBibleReadingGoalMember member : goalMemberRepository.findAllByAccountUid(
        accountUid)) {
      if (MEMBER_JOINED.equals(member.getStatus())) {
        goalIdxs.add(member.getGoalIdx());
      }
    }
    for (AccountBibleReadingGoal goal : goalRepository.findAllByAccountUidOrderByIdxDesc(
        accountUid)) {
      if (GOAL_IN_PROGRESS.equals(goal.getStatus())) {
        goalIdxs.add(goal.getIdx());
      }
    }
    for (Long goalIdx : goalIdxs) {
      if (goalRepository.existsById(goalIdx)) {
        bibleReadingService.leaveGoal(accountUid, goalIdx);
      }
    }
  }

  /** 탈퇴 계정인지 */
  public static boolean isWithdrawn(Account account) {
    return account.getLeaveYn() == Yn.Y;
  }
}
