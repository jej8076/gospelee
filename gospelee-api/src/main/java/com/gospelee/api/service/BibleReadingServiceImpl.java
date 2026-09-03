package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.biblereading.BibleReadingBookStatDTO;
import com.gospelee.api.dto.biblereading.BibleReadingCalendarDTO;
import com.gospelee.api.dto.biblereading.BibleReadingCheckRequestDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalRequestDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalResponseDTO;
import com.gospelee.api.dto.biblereading.BibleReadingStatusResponseDTO;
import com.gospelee.api.entity.AccountBibleRead;
import com.gospelee.api.entity.AccountBibleReadingGoal;
import com.gospelee.api.repository.jpa.account.AccountBibleReadRepository;
import com.gospelee.api.repository.jpa.account.AccountBibleReadingGoalRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import com.gospelee.api.utils.BibleUtils;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
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

    AccountBibleReadingGoal goal = AccountBibleReadingGoal.builder()
        .accountUid(account.getUid())
        .title(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle() : "성경 통독")
        .rangeType(rangeType)
        .customBooks(customBooksStr)
        .startDate(startDate)
        .targetDate(targetDate)
        .targetDays(targetDays)
        .totalChapters(totalChapters)
        .status("PROGRESS")
        .build();

    AccountBibleReadingGoal savedGoal = goalRepository.save(goal);

    // 새 목표는 0장부터 시작
    return BibleReadingGoalResponseDTO.fromEntity(savedGoal, 0, 0.0);
  }

  @Override
  @Transactional(readOnly = true)
  public BibleReadingGoalResponseDTO getActiveGoal() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    return goalRepository.findFirstByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS")
        .map(goal -> {
          Map<Integer, Integer> map = getCompletedCountMap(account.getUid(), goal.getIdx());
          int completed = calculateGoalCompletedChapters(goal, map);
          double rate = calculateProgressRate(completed, goal.getTotalChapters());
          return BibleReadingGoalResponseDTO.fromEntity(goal, completed, rate);
        })
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BibleReadingGoalResponseDTO> getActiveGoals() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    List<AccountBibleReadingGoal> activeGoals = goalRepository
        .findAllByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS");

    if (activeGoals.isEmpty()) {
      return Collections.emptyList();
    }

    return activeGoals.stream().map(goal -> {
      Map<Integer, Integer> completedCountMap = getCompletedCountMap(account.getUid(), goal.getIdx());
      int goalCompleted = calculateGoalCompletedChapters(goal, completedCountMap);
      double goalRate = calculateProgressRate(goalCompleted, goal.getTotalChapters());
      return BibleReadingGoalResponseDTO.fromEntity(goal, goalCompleted, goalRate);
    }).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void cancelGoal(Long goalIdx) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    AccountBibleReadingGoal goal = goalRepository.findById(goalIdx)
        .orElseThrow(() -> new NoSuchElementException("목표를 찾을 수 없습니다: " + goalIdx));

    if (!goal.getAccountUid().equals(account.getUid())) {
      throw new IllegalArgumentException("본인의 목표만 취소할 수 있습니다.");
    }

    goal.cancel();
    goalRepository.save(goal);
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
      Optional<AccountBibleReadingGoal> activeGoalOpt = goalRepository
          .findFirstByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS");
      goalIdx = activeGoalOpt.map(AccountBibleReadingGoal::getIdx).orElse(null);
    }

    String action = request.getAction() != null ? request.getAction().toUpperCase() : "READ";

    if ("UNREAD".equals(action)) {
      if (goalIdx != null) {
        readRepository.deleteByAccountUidAndGoalIdxAndBookAndChapterIn(account.getUid(), goalIdx, book, chapters);
      } else {
        readRepository.deleteByAccountUidAndBookAndChapterIn(account.getUid(), book, chapters);
      }
    } else {
      LocalDate readDate = request.getReadDate() != null ? request.getReadDate() : LocalDate.now();
      int cate = BibleUtils.getCateByBook(book);

      for (Integer chapter : chapters) {
        if (chapter == null || chapter < 1 || chapter > BibleUtils.getChaptersByBook(book)) {
          continue;
        }

        // 목표별 중복 체크
        Optional<AccountBibleRead> existing = (goalIdx != null)
            ? readRepository.findFirstByAccountUidAndGoalIdxAndBookAndChapter(account.getUid(), goalIdx, book, chapter)
            : readRepository.findFirstByAccountUidAndBookAndChapter(account.getUid(), book, chapter);

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
      if (goalIdx != null) {
        final Long targetGoalIdx = goalIdx;
        goalRepository.findById(targetGoalIdx).ifPresent(goal -> {
          if ("PROGRESS".equals(goal.getStatus())) {
            checkAndCompleteGoalIfFinished(account.getUid(), goal);
          }
        });
      } else {
        List<AccountBibleReadingGoal> activeGoals = goalRepository
            .findAllByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS");
        for (AccountBibleReadingGoal activeGoal : activeGoals) {
          checkAndCompleteGoalIfFinished(account.getUid(), activeGoal);
        }
      }
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

    // 활성 목표 목록 조회
    List<AccountBibleReadingGoal> activeGoals = goalRepository
        .findAllByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS");

    // 각 활성 목표별 진행률 계산하여 DTO 생성
    List<BibleReadingGoalResponseDTO> goalDTOList = new ArrayList<>();
    AccountBibleReadingGoal selectedGoal = null;
    BibleReadingGoalResponseDTO selectedGoalDTO = null;

    for (AccountBibleReadingGoal g : activeGoals) {
      Map<Integer, Integer> goalMap = getCompletedCountMap(account.getUid(), g.getIdx());
      int goalCompleted = calculateGoalCompletedChapters(g, goalMap);
      double goalRate = calculateProgressRate(goalCompleted, g.getTotalChapters());
      BibleReadingGoalResponseDTO dto = BibleReadingGoalResponseDTO.fromEntity(g, goalCompleted, goalRate);
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
          .completedChaptersList(null)
          .build());
    }

    // 선택된 목표에 따른 진행 통계 (목표가 없으면 0)
    int targetTotalChapters = 0;
    int targetCompletedChapters = 0;
    Long daysElapsed = null;
    double progressRate = 0.0;

    if (selectedGoal != null && selectedGoalDTO != null) {
      targetTotalChapters = selectedGoal.getTotalChapters();
      targetCompletedChapters = selectedGoalDTO.getCompletedChapters();
      daysElapsed = selectedGoalDTO.getDaysElapsed();
      progressRate = selectedGoalDTO.getProgressRate();
    }

    return BibleReadingStatusResponseDTO.builder()
        .activeGoal(selectedGoalDTO)
        .goals(goalDTOList)
        .totalChapters(targetTotalChapters)
        .completedChapters(targetCompletedChapters)
        .progressRate(progressRate)
        .daysElapsed(daysElapsed)
        .oldTestamentCompleted(oldCompleted)
        .newTestamentCompleted(newCompleted)
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
    List<AccountBibleRead> reads = (goalIdx != null)
        ? readRepository.findAllByAccountUidAndGoalIdxAndBook(account.getUid(), goalIdx, book)
        : readRepository.findAllByAccountUidAndBook(account.getUid(), book);
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

    List<AccountBibleRead> monthReads = readRepository.findAllByAccountUidAndReadDateOrderByBookAscChapterAsc(
        account.getUid(), start); // or between

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
