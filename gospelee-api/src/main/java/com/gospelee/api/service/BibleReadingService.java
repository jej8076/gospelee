package com.gospelee.api.service;

import com.gospelee.api.dto.biblereading.BibleReadingCalendarDTO;
import com.gospelee.api.dto.biblereading.BibleReadingCheckRequestDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalRequestDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalResponseDTO;
import com.gospelee.api.dto.biblereading.BibleReadingStatusResponseDTO;
import java.util.List;

public interface BibleReadingService {

  // 목표 생성
  BibleReadingGoalResponseDTO createGoal(BibleReadingGoalRequestDTO request);

  // 현재 진행 중인 목표 조회 (최신 순 1개)
  BibleReadingGoalResponseDTO getActiveGoal();

  // 현재 진행 중인 목표 전체 목록 조회
  List<BibleReadingGoalResponseDTO> getActiveGoals();

  // 목표 포기/취소
  void cancelGoal(Long goalIdx);

  // 장 다중 읽음 / 해제 처리
  void checkChapters(BibleReadingCheckRequestDTO request);

  // 통독 전체 현황 및 진도율 조회 (기본)
  BibleReadingStatusResponseDTO getStatus();

  // 통독 전체 현황 및 특정 목표 진도율 조회
  BibleReadingStatusResponseDTO getStatus(Long goalIdx);

  // 특정 책에서 읽은 장 목록 조회 (전체/기본)
  List<Integer> getReadChaptersByBook(int book);

  // 특정 목표 내 특정 책에서 읽은 장 목록 조회
  List<Integer> getReadChaptersByBook(int book, Long goalIdx);

  // 캘린더용 월별 통독 기록 조회
  List<BibleReadingCalendarDTO> getMonthlyCalendar(int year, int month);
}
