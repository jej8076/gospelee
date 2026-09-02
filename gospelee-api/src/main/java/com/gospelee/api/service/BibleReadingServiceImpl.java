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

    // 기존 진행 중인 목표가 있다면 취소 처리
    goalRepository.findFirstByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS")
        .ifPresent(existingGoal -> {
          existingGoal.cancel();
          goalRepository.save(existingGoal);
        });

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
    return BibleReadingGoalResponseDTO.fromEntity(savedGoal);
  }

  @Override
  @Transactional(readOnly = true)
  public BibleReadingGoalResponseDTO getActiveGoal() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    return goalRepository.findFirstByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS")
        .map(BibleReadingGoalResponseDTO::fromEntity)
        .orElse(null);
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

    Optional<AccountBibleReadingGoal> activeGoalOpt = goalRepository
        .findFirstByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS");
    Long goalIdx = activeGoalOpt.map(AccountBibleReadingGoal::getIdx).orElse(null);

    String action = request.getAction() != null ? request.getAction().toUpperCase() : "READ";

    if ("UNREAD".equals(action)) {
      readRepository.deleteByAccountUidAndBookAndChapterIn(account.getUid(), book, chapters);
    } else {
      LocalDate readDate = request.getReadDate() != null ? request.getReadDate() : LocalDate.now();
      int cate = BibleUtils.getCateByBook(book);

      for (Integer chapter : chapters) {
        if (chapter == null || chapter < 1 || chapter > BibleUtils.getChaptersByBook(book)) {
          continue;
        }

        // 중복 체크
        Optional<AccountBibleRead> existing = readRepository
            .findFirstByAccountUidAndBookAndChapter(account.getUid(), book, chapter);

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
      if (activeGoalOpt.isPresent()) {
        AccountBibleReadingGoal activeGoal = activeGoalOpt.get();
        checkAndCompleteGoalIfFinished(account.getUid(), activeGoal);
      }
    }
  }

  private void checkAndCompleteGoalIfFinished(Long accountUid, AccountBibleReadingGoal goal) {
    Set<Integer> targetBooks = getTargetBooksForGoal(goal);
    List<Object[]> completedList = readRepository.getCompletedChaptersByBook(accountUid);

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

  @Override
  @Transactional(readOnly = true)
  public BibleReadingStatusResponseDTO getStatus() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    // 활성 목표 조회
    Optional<AccountBibleReadingGoal> activeGoalOpt = goalRepository
        .findFirstByAccountUidAndStatusOrderByIdxDesc(account.getUid(), "PROGRESS");
    BibleReadingGoalResponseDTO activeGoalDTO = activeGoalOpt
        .map(BibleReadingGoalResponseDTO::fromEntity)
        .orElse(null);

    // 사용자 전체 읽은 기록 목록
    List<Object[]> completedByBook = readRepository.getCompletedChaptersByBook(account.getUid());
    Map<Integer, Integer> completedCountMap = new HashMap<>();
    for (Object[] row : completedByBook) {
      int b = ((Number) row[0]).intValue();
      int cnt = ((Number) row[1]).intValue();
      completedCountMap.put(b, cnt);
    }

    // 66권 전체 책별 통계 생성
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

    // 현재 목표에 따른 대상 장 수 및 완료 장 수 계산
    int targetTotalChapters = BibleUtils.TOTAL_CHAPTERS;
    int targetCompletedChapters = allCompletedChapters;
    Long daysElapsed = null;

    if (activeGoalOpt.isPresent()) {
      AccountBibleReadingGoal goal = activeGoalOpt.get();
      targetTotalChapters = goal.getTotalChapters();
      daysElapsed = activeGoalDTO.getDaysElapsed();

      Set<Integer> targetBooks = getTargetBooksForGoal(goal);
      targetCompletedChapters = 0;
      for (int b : targetBooks) {
        targetCompletedChapters += completedCountMap.getOrDefault(b, 0);
      }
    }

    double progressRate = targetTotalChapters > 0
        ? Math.round(((double) targetCompletedChapters / targetTotalChapters * 1000.0)) / 10.0
        : 0.0;
    if (progressRate > 100.0) progressRate = 100.0;

    return BibleReadingStatusResponseDTO.builder()
        .activeGoal(activeGoalDTO)
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
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    List<AccountBibleRead> reads = readRepository.findAllByAccountUidAndBook(account.getUid(), book);
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
