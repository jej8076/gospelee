package com.gospelee.api.controller;

import com.gospelee.api.dto.biblereading.BibleReadingCalendarDTO;
import com.gospelee.api.dto.biblereading.BibleReadingCheckRequestDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalRequestDTO;
import com.gospelee.api.dto.biblereading.BibleReadingGoalResponseDTO;
import com.gospelee.api.dto.biblereading.BibleReadingStatusResponseDTO;
import com.gospelee.api.dto.common.DataResponseDTO;
import com.gospelee.api.dto.common.ResponseDTO;
import com.gospelee.api.service.BibleReadingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bible/reading")
public class BibleReadingController {

  private final BibleReadingService bibleReadingService;

  /**
   * 현재 진행 중인 통독 목표 조회
   */
  @GetMapping("/goal/active")
  public ResponseEntity<DataResponseDTO<BibleReadingGoalResponseDTO>> getActiveGoal() {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.getActiveGoal())
    );
  }

  /**
   * 현재 진행 중인 모든 통독 목표 목록 조회
   */
  @GetMapping("/goals")
  public ResponseEntity<DataResponseDTO<List<BibleReadingGoalResponseDTO>>> getActiveGoals() {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.getActiveGoals())
    );
  }

  /**
   * 통독 목표 생성
   */
  @PostMapping("/goal")
  public ResponseEntity<DataResponseDTO<BibleReadingGoalResponseDTO>> createGoal(
      @RequestBody @Valid BibleReadingGoalRequestDTO request) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.createGoal(request))
    );
  }

  /**
   * 통독 목표 취소/포기
   */
  @DeleteMapping("/goal/{goalIdx}")
  public ResponseEntity<ResponseDTO> cancelGoal(@PathVariable("goalIdx") Long goalIdx) {
    bibleReadingService.cancelGoal(goalIdx);
    return ResponseEntity.ok(ResponseDTO.of("100", "성공"));
  }

  /**
   * 장 다중 선택 읽음 완료 / 해제
   */
  @PostMapping("/check")
  public ResponseEntity<ResponseDTO> checkChapters(
      @RequestBody @Valid BibleReadingCheckRequestDTO request) {
    bibleReadingService.checkChapters(request);
    return ResponseEntity.ok(ResponseDTO.of("100", "성공"));
  }

  /**
   * 통독 전체 상태 및 진도율 조회
   */
  @GetMapping("/status")
  public ResponseEntity<DataResponseDTO<BibleReadingStatusResponseDTO>> getStatus(
      @RequestParam(value = "goalIdx", required = false) Long goalIdx) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.getStatus(goalIdx))
    );
  }

  /**
   * 특정 성경 책(권)의 읽은 장 번호 목록 조회 (목표별)
   */
  @GetMapping("/book/{book}")
  public ResponseEntity<DataResponseDTO<List<Integer>>> getReadChaptersByBook(
      @PathVariable("book") int book,
      @RequestParam(value = "goalIdx", required = false) Long goalIdx) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.getReadChaptersByBook(book, goalIdx))
    );
  }

  /**
   * 캘린더용 월별 통독 기록 조회
   */
  @GetMapping("/calendar/{year}/{month}")
  public ResponseEntity<DataResponseDTO<List<BibleReadingCalendarDTO>>> getMonthlyCalendar(
      @PathVariable("year") int year, @PathVariable("month") int month) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.getMonthlyCalendar(year, month))
    );
  }

  /**
   * 초대 코드로 목표 정보 사전 조회 (비회원/회원 모두 가능)
   */
  @GetMapping("/goal/invite/{code}")
  public ResponseEntity<DataResponseDTO<com.gospelee.api.dto.biblereading.BibleReadingGoalInviteInfoDTO>> getInviteInfo(
      @PathVariable("code") String code) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.getInviteInfo(code))
    );
  }

  /**
   * 초대 코드로 목표 참여
   */
  @PostMapping("/goal/join")
  public ResponseEntity<DataResponseDTO<BibleReadingGoalResponseDTO>> joinGoal(
      @RequestBody @Valid com.gospelee.api.dto.biblereading.BibleReadingJoinRequestDTO request) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.joinGoal(request.getInviteCode()))
    );
  }

  /**
   * 목표 참여자 목록 및 진도율 조회
   */
  @GetMapping("/goal/{goalIdx}/members")
  public ResponseEntity<DataResponseDTO<List<com.gospelee.api.dto.biblereading.BibleReadingMemberDTO>>> getGoalMembers(
      @PathVariable("goalIdx") Long goalIdx) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", bibleReadingService.getGoalMembers(goalIdx))
    );
  }

  /**
   * 목표 나가기 (참여자 탈퇴 / 방장 위임)
   */
  @PostMapping("/goal/{goalIdx}/leave")
  public ResponseEntity<ResponseDTO> leaveGoal(@PathVariable("goalIdx") Long goalIdx) {
    bibleReadingService.leaveGoal(goalIdx);
    return ResponseEntity.ok(ResponseDTO.of("100", "성공"));
  }
}
