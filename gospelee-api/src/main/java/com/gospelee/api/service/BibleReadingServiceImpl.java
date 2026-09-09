package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.biblereading.BibleReadingBookStatDTO;
import com.gospelee.api.dto.biblereading.BibleReadingCalendarDTO;
import com.gospelee.api.dto.biblereading.BibleReadingCheckRequestDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalInviteInfoDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalRequestDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalResponseDTO;
import com.gospelee.api.dto.biblereading.BibleReadingMemberDTO;
import com.gospelee.api.dto.biblereading.BibleReadingStatusResponseDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountBibleRead;
import com.gospelee.api.entity.AccountBibleReadingGoal;
import com.gospelee.api.entity.AccountBibleReadingGoalMember;
import com.gospelee.api.repository.jpa.account.AccountBibleReadRepository;
import com.gospelee.api.repository.jpa.account.AccountBibleReadingGoalMemberRepository;
import com.gospelee.api.repository.jpa.account.AccountBibleReadingGoalRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import com.gospelee.api.utils.BibleUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BibleReadingServiceImpl implements BibleReadingService {

  private final AccountBibleReadingGoalRepository goalRepository;
  private final AccountBibleReadRepository readRepository;
  private final AccountBibleReadingGoalMemberRepository memberRepository;
  private final AccountRepository accountRepository;

  @Override
  @Transactional
  public BibleReadingGoalResponseDTO createGoal(BibleReadingGoalRequestDTO request) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    // 다중 목표 지원: 기존 목표를 강제 취소하지 않고 독립적으로 새 목표를 생성함
    LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
    LocalDate targetDate = request.getTargetDate();
    Integer targetDays = request.getTargetDays();

    if (targetDays != null && targetDays > 0 && targetDate == null) {
      targetDate = startDate.plusDays(targetDays - 1);
    } else if (targetDate != null && targetDays == null) {
      targetDays = (int) ChronoUnit.DAYS.between(startDate, targetDate) + 1;
    }

    // 총 장 수 계산
    int totalChapters;
    String customBooksStr = null;
    String rangeType = request.getRangeType() != null ? request.getRangeType().toUpperCase() : "ALL";

    switch (rangeType) {
      case "OLD":
        totalChapters = BibleUtils.OLD_TESTAMENT_CHAPTERS;
        break;
      case "NEW":
        totalChapters = BibleUtils.NEW_TESTAMENT_CHAPTERS;
        break;
      case "CUSTOM":
        if (request.getCustomBooks() != null && !request.getCustomBooks().isEmpty()) {
          customBooksStr = request.getCustomBooks().stream()
              .map(String::valueOf)
              .collect(Collectors.joining(","));
          totalChapters = request.getCustomBooks().stream()
              .mapToInt(BibleUtils::getChaptersByBook)
              .sum();
        } else {
          totalChapters = BibleUtils.TOTAL_CHAPTERS;
          rangeType = "ALL";
        }
        break;
      case "ALL":
      default:
        rangeType = "ALL";
        totalChapters = BibleUtils.TOTAL_CHAPTERS;
        break;
    }

    String orderType = request.getOrderType() != null && !request.getOrderType().isBlank()
        ? request.getOrderType().toUpperCase()
        : "CANONICAL";

    if ("CUSTOM".equals(rangeType) && "CANONICAL".equals(orderType)) {
      orderType = "CUSTOM";
    }

    String inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

    AccountBibleReadingGoal goal = AccountBibleReadingGoal.builder()
        .accountUid(account.getUid())
        .title(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle() : "성경 통독")
        .rangeType(rangeType)
        .orderType(orderType)
        .customBooks(customBooksStr)
        .startDate(startDate)
        .targetDate(targetDate)
        .targetDays(targetDays)
        .totalChapters(totalChapters)
        .status("PROGRESS")
        .inviteCode(inviteCode)
        .build();

    AccountBibleReadingGoal savedGoal = goalRepository.save(goal);

    // 방장(HOST)으로 첫 멤버 등록
    AccountBibleReadingGoalMember hostMember = AccountBibleReadingGoalMember.builder()
        .goalIdx(savedGoal.getIdx())
        .accountUid(account.getUid())
        .role("HOST")
        .status("JOINED")
        .joinedAt(LocalDateTime.now())
        .build();
    memberRepository.save(hostMember);

    // 새 목표는 0장부터 시작 (참여자 1명, 방장)
    return BibleReadingGoalResponseDTO.fromEntity(savedGoal, 0, 0.0, 1, true);
  }

  /**
   * 사용자가 참여 중인 모든 활성 목표(본인 생성 + 멤버 참여) 조회
   */
  private List<AccountBibleReadingGoal> getActiveGoalsForUser(Long accountUid) {
    // 1. 멤버로 참여 중인 목표 목록
    List<AccountBibleReadingGoalMember> memberships = memberRepository
        .findAllByAccountUidAndStatusOrderByJoinedAtDesc(accountUid, "JOINED");
    Set<Long> memberGoalIds = memberships.stream()
        .map(AccountBibleReadingGoalMember::getGoalIdx)
        .collect(Collectors.toSet());

    // 2. 본인이 생성한 목표 목록
    List<AccountBibleReadingGoal> createdGoals = goalRepository
        .findAllByAccountUidAndStatusOrderByIdxDesc(accountUid, "PROGRESS");

    Map<Long, AccountBibleReadingGoal> goalMap = new LinkedHashMap<>();
    for (AccountBibleReadingGoal g : createdGoals) {
      goalMap.put(g.getIdx(), g);
    }

    for (Long gId : memberGoalIds) {
      if (!goalMap.containsKey(gId)) {
        goalRepository.findById(gId).ifPresent(g -> {
          if ("PROGRESS".equals(g.getStatus())) {
            goalMap.put(g.getIdx(), g);
          }
        });
      }
    }

    // inviteCode가 없는 레거시 목표가 있다면 생성
    for (AccountBibleReadingGoal g : goalMap.values()) {
      ensureInviteCode(g);
    }

    return new ArrayList<>(goalMap.values());
  }

  private void ensureInviteCode(AccountBibleReadingGoal goal) {
    if (goal.getInviteCode() == null || goal.getInviteCode().isBlank()) {
      String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
      goal.changeInviteCode(code);
      goalRepository.save(goal);
    }
  }

  private boolean isUserHost(AccountBibleReadingGoal goal, Long accountUid) {
    Optional<AccountBibleReadingGoalMember> memberOpt = memberRepository
        .findByGoalIdxAndAccountUid(goal.getIdx(), accountUid);
    if (memberOpt.isPresent()) {
      return "HOST".equals(memberOpt.get().getRole());
    }
    return goal.getAccountUid().equals(accountUid);
  }

  private int getParticipantCount(Long goalIdx) {
    int count = memberRepository.countByGoalIdxAndStatus(goalIdx, "JOINED");
    return count > 0 ? count : 1;
  }

  @Override
  @Transactional(readOnly = true)
  public BibleReadingGoalResponseDTO getActiveGoal() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    List<AccountBibleReadingGoal> activeGoals = getActiveGoalsForUser(account.getUid());
    if (activeGoals.isEmpty()) {
      return null;
    }
    AccountBibleReadingGoal goal = activeGoals.get(0);
    Map<Integer, Integer> map = getCompletedCountMap(account.getUid(), goal.getIdx());
    int completed = calculateGoalCompletedChapters(goal, map);
    double rate = calculateProgressRate(completed, goal.getTotalChapters());
    int participantCount = getParticipantCount(goal.getIdx());
    boolean isHost = isUserHost(goal, account.getUid());
    return BibleReadingGoalResponseDTO.fromEntity(goal, completed, rate, participantCount, isHost);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BibleReadingGoalResponseDTO> getActiveGoals() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    List<AccountBibleReadingGoal> activeGoals = getActiveGoalsForUser(account.getUid());

    if (activeGoals.isEmpty()) {
      return Collections.emptyList();
    }

    return activeGoals.stream().map(goal -> {
      Map<Integer, Integer> completedCountMap = getCompletedCountMap(account.getUid(), goal.getIdx());
      int goalCompleted = calculateGoalCompletedChapters(goal, completedCountMap);
      double goalRate = calculateProgressRate(goalCompleted, goal.getTotalChapters());
      int participantCount = getParticipantCount(goal.getIdx());
      boolean isHost = isUserHost(goal, account.getUid());
      return BibleReadingGoalResponseDTO.fromEntity(goal, goalCompleted, goalRate, participantCount, isHost);
    }).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void cancelGoal(Long goalIdx) {
    leaveGoal(goalIdx);
  }

  @Override
  @Transactional
  public void leaveGoal(Long goalIdx) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    AccountBibleReadingGoal goal = goalRepository.findById(goalIdx)
        .orElseThrow(() -> new NoSuchElementException("목표를 찾을 수 없습니다: " + goalIdx));

    Optional<AccountBibleReadingGoalMember> memberOpt = memberRepository
        .findByGoalIdxAndAccountUid(goalIdx, account.getUid());

    AccountBibleReadingGoalMember currentMember;
    if (memberOpt.isEmpty()) {
      if (goal.getAccountUid().equals(account.getUid())) {
        currentMember = AccountBibleReadingGoalMember.builder()
            .goalIdx(goalIdx)
            .accountUid(account.getUid())
            .role("HOST")
            .status("JOINED")
            .joinedAt(LocalDateTime.now())
            .build();
        currentMember = memberRepository.save(currentMember);
      } else {
        throw new IllegalArgumentException("해당 목표의 참여자가 아닙니다.");
      }
    } else {
      currentMember = memberOpt.get();
    }

    // 본인의 해당 목표 읽음 기록 삭제
    readRepository.deleteByAccountUidAndGoalIdx(account.getUid(), goalIdx);

    boolean isHost = "HOST".equals(currentMember.getRole()) || goal.getAccountUid().equals(account.getUid());

    if (isHost) {
      // B안: 방장이 나갈 경우 다음 참여자에게 방장 권한 위임
      Optional<AccountBibleReadingGoalMember> nextMemberOpt = memberRepository
          .findFirstByGoalIdxAndStatusAndRoleOrderByJoinedAtAsc(goalIdx, "JOINED", "MEMBER");

      if (nextMemberOpt.isPresent()) {
        AccountBibleReadingGoalMember nextHost = nextMemberOpt.get();
        nextHost.changeRole("HOST");
        memberRepository.save(nextHost);

        goal.changeAccountUid(nextHost.getAccountUid());
        goalRepository.save(goal);

        currentMember.changeStatus("LEFT");
        memberRepository.save(currentMember);
      } else {
        // 남은 다른 참여자가 없으면 목표 취소/종료 처리
        goal.cancel();
        goalRepository.save(goal);

        currentMember.changeStatus("LEFT");
        memberRepository.save(currentMember);
      }
    } else {
      // 일반 참여자 나가기
      currentMember.changeStatus("LEFT");
      memberRepository.save(currentMember);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public BibleReadingGoalInviteInfoDTO getInviteInfo(String inviteCode) {
    AccountBibleReadingGoal goal = goalRepository.findByInviteCode(inviteCode)
        .orElseThrow(() -> new NoSuchElementException("유효하지 않은 초대 코드입니다."));

    if (!"PROGRESS".equals(goal.getStatus())) {
      throw new IllegalStateException("이미 종료되었거나 진행 중이지 않은 통독 목표입니다.");
    }

    String hostName = accountRepository.findById(goal.getAccountUid())
        .map(Account::getName)
        .orElse("성도");

    int participantCount = getParticipantCount(goal.getIdx());

    boolean isAlreadyJoined = false;
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrNull();
    if (account != null) {
      isAlreadyJoined = memberRepository
          .existsByGoalIdxAndAccountUidAndStatus(goal.getIdx(), account.getUid(), "JOINED");
      if (!isAlreadyJoined && goal.getAccountUid().equals(account.getUid())) {
        isAlreadyJoined = true;
      }
    }

    String rangeTypeLabel = getRangeTypeLabel(goal.getRangeType(), goal.getCustomBooks());
    String orderTypeLabel = getOrderTypeLabel(goal.getOrderType());

    return BibleReadingGoalInviteInfoDTO.builder()
        .goalIdx(goal.getIdx())
        .title(goal.getTitle())
        .rangeType(goal.getRangeType())
        .rangeTypeLabel(rangeTypeLabel)
        .orderType(goal.getOrderType())
        .orderTypeLabel(orderTypeLabel)
        .startDate(goal.getStartDate())
        .targetDate(goal.getTargetDate())
        .targetDays(goal.getTargetDays())
        .totalChapters(goal.getTotalChapters())
        .participantCount(participantCount)
        .hostName(hostName)
        .inviteCode(inviteCode)
        .isAlreadyJoined(isAlreadyJoined)
        .build();
  }

  @Override
  @Transactional
  public BibleReadingGoalResponseDTO joinGoal(String inviteCode) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    AccountBibleReadingGoal goal = goalRepository.findByInviteCode(inviteCode)
        .orElseThrow(() -> new NoSuchElementException("유효하지 않은 초대 코드입니다."));

    if (!"PROGRESS".equals(goal.getStatus())) {
      throw new IllegalStateException("진행 중이지 않은 통독 목표에는 참여할 수 없습니다.");
    }

    Optional<AccountBibleReadingGoalMember> existingMember = memberRepository
        .findByGoalIdxAndAccountUid(goal.getIdx(), account.getUid());

    boolean isHost = goal.getAccountUid().equals(account.getUid());

    if (existingMember.isPresent()) {
      AccountBibleReadingGoalMember member = existingMember.get();
      if ("LEFT".equals(member.getStatus())) {
        member.rejoin();
        memberRepository.save(member);
      }
      isHost = "HOST".equals(member.getRole());
    } else {
      AccountBibleReadingGoalMember newMember = AccountBibleReadingGoalMember.builder()
          .goalIdx(goal.getIdx())
          .accountUid(account.getUid())
          .role(isHost ? "HOST" : "MEMBER")
          .status("JOINED")
          .joinedAt(LocalDateTime.now())
          .build();
      memberRepository.save(newMember);
    }

    // 통독 목표의 날짜는 변하지 않고, 참여자는 0장부터 시작함 (요구사항 3)
    int participantCount = getParticipantCount(goal.getIdx());
    Map<Integer, Integer> map = getCompletedCountMap(account.getUid(), goal.getIdx());
    int completed = calculateGoalCompletedChapters(goal, map);
    double rate = calculateProgressRate(completed, goal.getTotalChapters());

    return BibleReadingGoalResponseDTO.fromEntity(goal, completed, rate, participantCount, isHost);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BibleReadingMemberDTO> getGoalMembers(Long goalIdx) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    AccountBibleReadingGoal goal = goalRepository.findById(goalIdx)
        .orElseThrow(() -> new NoSuchElementException("목표를 찾을 수 없습니다: " + goalIdx));

    List<AccountBibleReadingGoalMember> members = memberRepository
        .findAllByGoalIdxAndStatusOrderByJoinedAtAsc(goalIdx, "JOINED");

    // 레거시 목표 등 멤버 테이블에 아무도 없으면 방장 자동 추가
    if (members.isEmpty()) {
      AccountBibleReadingGoalMember hostMember = AccountBibleReadingGoalMember.builder()
          .goalIdx(goalIdx)
          .accountUid(goal.getAccountUid())
          .role("HOST")
          .status("JOINED")
          .joinedAt(goal.getInsertTime() != null ? goal.getInsertTime() : LocalDateTime.now())
          .build();
      members = Collections.singletonList(memberRepository.save(hostMember));
    }

    Set<Integer> targetBooks = getTargetBooksForGoal(goal);
    List<BibleReadingMemberDTO> dtoList = new ArrayList<>();

    for (AccountBibleReadingGoalMember m : members) {
      String name = accountRepository.findById(m.getAccountUid())
          .map(Account::getName)
          .orElse("성도");

      Map<Integer, Integer> memberMap = getCompletedCountMap(m.getAccountUid(), goalIdx);
      int completed = 0;
      for (int b : targetBooks) {
        completed += memberMap.getOrDefault(b, 0);
      }
      double rate = calculateProgressRate(completed, goal.getTotalChapters());
      LocalDate lastReadDate = readRepository.findLastReadDateByAccountUidAndGoalIdx(m.getAccountUid(), goalIdx);

      dtoList.add(BibleReadingMemberDTO.builder()
          .accountUid(m.getAccountUid())
          .name(name)
          .role(m.getRole())
          .completedChapters(completed)
          .progressRate(rate)
          .lastReadDate(lastReadDate)
          .joinedAt(m.getJoinedAt())
          .isMe(m.getAccountUid().equals(account.getUid()))
          .build());
    }

    // 정렬: 진도율 내림차순 -> 완료 장수 내림차순 -> 참여일 오름차순
    dtoList.sort((a, b) -> {
      int cmp = Double.compare(b.getProgressRate(), a.getProgressRate());
      if (cmp != 0) return cmp;
      cmp = Integer.compare(b.getCompletedChapters(), a.getCompletedChapters());
      if (cmp != 0) return cmp;
      if (a.getJoinedAt() != null && b.getJoinedAt() != null) {
        return a.getJoinedAt().compareTo(b.getJoinedAt());
      }
      return 0;
    });

    return dtoList;
  }

  private String getRangeTypeLabel(String rangeType, String customBooks) {
    if (rangeType == null) return "성경 전체 66권";
    switch (rangeType) {
      case "OLD":
        return "구약 39권";
      case "NEW":
        return "신약 27권";
      case "CUSTOM":
        int count = 0;
        if (customBooks != null && !customBooks.isBlank()) {
          count = customBooks.split(",").length;
        }
        return "직접 선택 (" + count + "권)";
      case "ALL":
      default:
        return "성경 전체 66권";
    }
  }

  private String getOrderTypeLabel(String orderType) {
    if (orderType == null) return "정경순";
    switch (orderType) {
      case "CHRONOLOGICAL":
        return "연대기순";
      case "NEW_FIRST":
        return "신약 우선";
      case "CUSTOM":
        return "직접 지정 순서";
      case "CANONICAL":
      default:
        return "정경순";
    }
  }

  @Override
  @Transactional
  public void checkChapters(BibleReadingCheckRequestDTO request) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    int book = request.getBook();
    List<Integer> chapters = request.getChapters();

    if (chapters == null || chapters.isEmpty()) {
      return;
    }

    Long goalIdx = request.getGoalIdx();
    if (goalIdx == null) {
      List<AccountBibleReadingGoal> activeGoals = getActiveGoalsForUser(account.getUid());
      goalIdx = activeGoals.isEmpty() ? null : activeGoals.get(0).getIdx();
    }

    if (goalIdx == null) {
      throw new IllegalStateException("진행 중인 통독 목표가 없습니다. 목표를 먼저 생성해주세요.");
    }

    String action = request.getAction() != null ? request.getAction().toUpperCase() : "READ";

    if ("UNREAD".equals(action)) {
      readRepository.deleteByAccountUidAndGoalIdxAndBookAndChapterIn(account.getUid(), goalIdx, book, chapters);
    } else {
      LocalDate readDate = request.getReadDate() != null ? request.getReadDate() : LocalDate.now();
      int cate = BibleUtils.getCateByBook(book);

      for (Integer chapter : chapters) {
        if (chapter == null || chapter < 1 || chapter > BibleUtils.getChaptersByBook(book)) {
          continue;
        }

        // 목표별 중복 체크
        Optional<AccountBibleRead> existing = readRepository
            .findFirstByAccountUidAndGoalIdxAndBookAndChapter(account.getUid(), goalIdx, book, chapter);

        if (existing.isEmpty()) {
          AccountBibleRead readRecord = AccountBibleRead.builder()
              .accountUid(account.getUid())
              .goalIdx(goalIdx)
              .cate(cate)
              .book(book)
              .chapter(chapter)
              .readDate(readDate)
              .build();
          readRepository.save(readRecord);
        }
      }

      // 목표 완료 여부 확인
      final Long targetGoalIdx = goalIdx;
      goalRepository.findById(targetGoalIdx).ifPresent(goal -> {
        if ("PROGRESS".equals(goal.getStatus())) {
          checkAndCompleteGoalIfFinished(account.getUid(), goal);
        }
      });
    }
  }

  private void checkAndCompleteGoalIfFinished(Long accountUid, AccountBibleReadingGoal goal) {
    Set<Integer> targetBooks = getTargetBooksForGoal(goal);
    List<Object[]> completedList = (goal.getIdx() != null)
        ? readRepository.getCompletedChaptersByGoal(accountUid, goal.getIdx())
        : readRepository.getCompletedChaptersByBook(accountUid);

    int totalCompletedInGoal = 0;
    for (Object[] row : completedList) {
      int b = ((Number) row[0]).intValue();
      int cnt = ((Number) row[1]).intValue();
      if (targetBooks.contains(b)) {
        totalCompletedInGoal += cnt;
      }
    }

    if (totalCompletedInGoal >= goal.getTotalChapters()) {
      goal.complete();
      goalRepository.save(goal);
    }
  }

  private Set<Integer> getTargetBooksForGoal(AccountBibleReadingGoal goal) {
    Set<Integer> books = new HashSet<>();
    String rangeType = goal.getRangeType() != null ? goal.getRangeType() : "ALL";
    switch (rangeType) {
      case "OLD":
        for (int i = 1; i <= BibleUtils.OLD_TESTAMENT_BOOKS; i++) books.add(i);
        break;
      case "NEW":
        for (int i = BibleUtils.OLD_TESTAMENT_BOOKS + 1; i <= 66; i++) books.add(i);
        break;
      case "CUSTOM":
        if (goal.getCustomBooks() != null && !goal.getCustomBooks().isBlank()) {
          Arrays.stream(goal.getCustomBooks().split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .map(Integer::parseInt)
              .forEach(books::add);
        }
        break;
      case "ALL":
      default:
        for (int i = 1; i <= 66; i++) books.add(i);
        break;
    }
    return books;
  }

  private Map<Integer, Integer> getCompletedCountMap(Long accountUid, Long goalIdx) {
    List<Object[]> completedByBook = (goalIdx != null)
        ? readRepository.getCompletedChaptersByGoal(accountUid, goalIdx)
        : readRepository.getCompletedChaptersByBook(accountUid);
    Map<Integer, Integer> map = new HashMap<>();
    for (Object[] row : completedByBook) {
      int b = ((Number) row[0]).intValue();
      int cnt = ((Number) row[1]).intValue();
      map.put(b, cnt);
    }
    return map;
  }

  private int calculateGoalCompletedChapters(AccountBibleReadingGoal goal, Map<Integer, Integer> completedCountMap) {
    Set<Integer> targetBooks = getTargetBooksForGoal(goal);
    int completed = 0;
    for (int b : targetBooks) {
      completed += completedCountMap.getOrDefault(b, 0);
    }
    return completed;
  }

  private double calculateProgressRate(int completed, int total) {
    if (total <= 0) return 0.0;
    double rate = Math.round(((double) completed / total * 1000.0)) / 10.0;
    return rate > 100.0 ? 100.0 : rate;
  }

  @Override
  @Transactional(readOnly = true)
  public BibleReadingStatusResponseDTO getStatus() {
    return getStatus(null);
  }

  @Override
  @Transactional(readOnly = true)
  public BibleReadingStatusResponseDTO getStatus(Long goalIdx) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    // 활성 목표 목록 조회 (본인 생성 + 멤버 참여)
    List<AccountBibleReadingGoal> activeGoals = getActiveGoalsForUser(account.getUid());

    // 각 활성 목표별 진행률 계산하여 DTO 생성
    List<BibleReadingGoalResponseDTO> goalDTOList = new ArrayList<>();
    AccountBibleReadingGoal selectedGoal = null;
    BibleReadingGoalResponseDTO selectedGoalDTO = null;

    for (AccountBibleReadingGoal g : activeGoals) {
      Map<Integer, Integer> goalMap = getCompletedCountMap(account.getUid(), g.getIdx());
      int goalCompleted = calculateGoalCompletedChapters(g, goalMap);
      double goalRate = calculateProgressRate(goalCompleted, g.getTotalChapters());
      int participantCount = getParticipantCount(g.getIdx());
      boolean isHost = isUserHost(g, account.getUid());
      BibleReadingGoalResponseDTO dto = BibleReadingGoalResponseDTO.fromEntity(g, goalCompleted, goalRate, participantCount, isHost);
      goalDTOList.add(dto);

      if (goalIdx != null && g.getIdx().equals(goalIdx)) {
        selectedGoal = g;
        selectedGoalDTO = dto;
      }
    }

    // goalIdx가 없거나 목록에 없으면 첫 번째 활성 목표를 기본 선택 (없으면 null)
    if (selectedGoal == null && !activeGoals.isEmpty()) {
      selectedGoal = activeGoals.get(0);
      selectedGoalDTO = goalDTOList.get(0);
    }

    // 선택된 목표 기준 완료 현황 맵
    Long selectedGoalIdx = selectedGoal != null ? selectedGoal.getIdx() : goalIdx;
    Map<Integer, Integer> completedCountMap = getCompletedCountMap(account.getUid(), selectedGoalIdx);

    // 66권 전체 책별 통계 생성 (선택된 목표 기준)
    List<BibleReadingBookStatDTO> bookStats = new ArrayList<>(66);
    int allCompletedChapters = 0;
    int oldCompleted = 0;
    int newCompleted = 0;

    for (int book = 1; book <= 66; book++) {
      int totalCh = BibleUtils.getChaptersByBook(book);
      int completedCh = completedCountMap.getOrDefault(book, 0);
      boolean isCompleted = completedCh >= totalCh && totalCh > 0;

      allCompletedChapters += completedCh;
      if (book <= BibleUtils.OLD_TESTAMENT_BOOKS) {
        oldCompleted += completedCh;
      } else {
        newCompleted += completedCh;
      }

      bookStats.add(BibleReadingBookStatDTO.builder()
          .book(book)
          .bookName(BibleUtils.getBookName(book))
          .totalChapters(totalCh)
          .completedChapters(completedCh)
          .isCompleted(isCompleted)
          .build());
    }

    // 목표 완독 여부 확인 (1189장 달성 시 자동 COMPLETED 처리)
    if (selectedGoal != null && "PROGRESS".equals(selectedGoal.getStatus())) {
      int goalCompleted = selectedGoalDTO != null ? selectedGoalDTO.getCompletedChapters() : 0;
      if (goalCompleted >= selectedGoal.getTotalChapters()) {
        selectedGoal.complete();
        goalRepository.save(selectedGoal);
      }
    }

    long daysElapsed = selectedGoalDTO != null ? selectedGoalDTO.getDaysElapsed() : 1L;
    double totalProgressRate = selectedGoalDTO != null ? selectedGoalDTO.getProgressRate() : 0.0;

    return BibleReadingStatusResponseDTO.builder()
        .activeGoal(selectedGoalDTO)
        .goals(goalDTOList)
        .completedChapters(allCompletedChapters)
        .progressRate(totalProgressRate)
        .oldTestamentCompleted(oldCompleted)
        .newTestamentCompleted(newCompleted)
        .totalChapters(BibleUtils.TOTAL_CHAPTERS)
        .daysElapsed(daysElapsed)
        .bookStats(bookStats)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Integer> getReadChaptersByBook(int book) {
    return getReadChaptersByBook(book, null);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Integer> getReadChaptersByBook(int book, Long goalIdx) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (goalIdx == null) {
      List<AccountBibleReadingGoal> activeGoals = getActiveGoalsForUser(account.getUid());
      goalIdx = activeGoals.isEmpty() ? null : activeGoals.get(0).getIdx();
      if (goalIdx == null) {
        return Collections.emptyList();
      }
    }
    List<AccountBibleRead> reads = readRepository
        .findAllByAccountUidAndGoalIdxAndBook(account.getUid(), goalIdx, book);
    return reads.stream()
        .map(AccountBibleRead::getChapter)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<BibleReadingCalendarDTO> getMonthlyCalendar(int year, int month) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.plusMonths(1).minusDays(1);

    // 월간 범위 내 날짜별 집계
    List<Object[]> dateCounts = readRepository.countReadByDateBetween(account.getUid(), start, end);
    Map<LocalDate, Integer> countMap = new HashMap<>();
    for (Object[] row : dateCounts) {
      LocalDate d = (LocalDate) row[0];
      int cnt = ((Number) row[1]).intValue();
      countMap.put(d, cnt);
    }

    List<BibleReadingCalendarDTO> calendarList = new ArrayList<>();
    for (Map.Entry<LocalDate, Integer> entry : countMap.entrySet()) {
      calendarList.add(BibleReadingCalendarDTO.builder()
          .date(entry.getKey())
          .count(entry.getValue())
          .chapters(Collections.emptyList())
          .build());
    }

    calendarList.sort((a, b) -> a.getDate().compareTo(b.getDate()));
    return calendarList;
  }
}
