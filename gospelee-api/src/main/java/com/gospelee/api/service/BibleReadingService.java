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

  // 초대 코드로 목표 정보 사전 조회 (비회원/회원 모두 가능)
  com.gospelee.api.dto.biblereading.BibleReadingGoalInviteInfoDTO getInviteInfo(String inviteCode);

  // 초대 코드로 목표 참여 (로그인 필요)
  BibleReadingGoalResponseDTO joinGoal(String inviteCode);

  // 목표 참여자 목록 및 진도율 조회
  List<com.gospelee.api.dto.biblereading.BibleReadingMemberDTO> getGoalMembers(Long goalIdx);

  // 목표 나가기 (참여자/방장 위임 처리)
  void leaveGoal(Long goalIdx);
}
