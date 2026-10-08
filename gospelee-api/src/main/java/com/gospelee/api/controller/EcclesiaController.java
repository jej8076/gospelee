package com.gospelee.api.controller;

import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.common.SearchDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteJoinRequestDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteSettingsRequestDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaUpdateDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaVerifyRequestDTO;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.service.EcclesiaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/ecclesia")
public class EcclesiaController {

  private final EcclesiaService ecclesiaService;

  @PostMapping("/list")
  public ResponseEntity<Object> getEcclesias() {
    List<EcclesiaResponseDTO> ecclesiaList = ecclesiaService.getEcclesiaList();
    return new ResponseEntity<>(ecclesiaList, HttpStatus.OK);
  }

  @PostMapping("/search")
  public ResponseEntity<Object> searchEcclesia(@RequestBody SearchDTO searchDTO) {
    List<EcclesiaResponseDTO> ecclesiaList = ecclesiaService.searchEcclesia(searchDTO.getText());
    return new ResponseEntity<>(ecclesiaList, HttpStatus.OK);
  }

  @PostMapping("/{ecclesiaUid}")
  public ResponseEntity<Object> getEcclesia(@PathVariable("ecclesiaUid") Long ecclesiaUid) {
    Ecclesia ecclesia = ecclesiaService.getEcclesia(ecclesiaUid);
    return new ResponseEntity<>(ecclesia, HttpStatus.OK);
  }

  @PostMapping("/account/{accountUid}")
  public ResponseEntity<Object> getEcclesiaByAccount(@PathVariable("accountUid") Long accountUid) {
    Ecclesia ecclesia = ecclesiaService.getEcclesiaByAccountUid(accountUid);
    return new ResponseEntity<>(ecclesia, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> insertEcclesia(@RequestBody EcclesiaInsertDTO ecclesiaInsertDTO) {
    Ecclesia ecclesia = ecclesiaService.saveEcclesia(ecclesiaInsertDTO);
    return new ResponseEntity<>(ecclesia, HttpStatus.OK);
  }

  // 운영자(ADMIN)만 호출 가능: 전화 등으로 확인한 교회를 검증 완료로 표시
  @PatchMapping("/verify")
  public ResponseEntity<Object> verifyEcclesia(@RequestBody EcclesiaVerifyRequestDTO request) {
    return new ResponseEntity<>(ecclesiaService.updateVerification(request), HttpStatus.OK);
  }

  @PatchMapping("/status")
  public ResponseEntity<Object> updateEcclesia(@RequestBody EcclesiaUpdateDTO ecclesiaUpdateDTO) {
    EcclesiaResponseDTO responseDTO = ecclesiaService.updateEcclesia(ecclesiaUpdateDTO);
    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }

  /**
   * 교회 참여 요청
   *
   * @param ecclesiaUid
   * @return
   */
  @PostMapping("/join/request/{ecclesiaUid}")
  public ResponseEntity<Object> joinRequestEcclesia(@PathVariable("ecclesiaUid") Long ecclesiaUid) {
    AccountEcclesiaHistory result = ecclesiaService.joinRequestEcclesia(ecclesiaUid);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * 내 교회 가입 요청 상태 조회 (PENDING / REJECTED / NONE)
   */
  @PostMapping("/join/request/my")
  public ResponseEntity<Object> myJoinRequestStatus() {
    return new ResponseEntity<>(ecclesiaService.getMyJoinRequestStatus(), HttpStatus.OK);
  }

  /**
   * 교회 가입 요청 취소 (반려된 요청 확인 처리 포함)
   */
  @PostMapping("/join/request/cancel")
  public ResponseEntity<Object> cancelJoinRequest() {
    ecclesiaService.cancelJoinRequest();
    return new ResponseEntity<>(
        com.gospelee.api.dto.common.ResponseDTO.of("100", "성공"), HttpStatus.OK);
  }

  /**
   * 교회 초대 설정 조회 (교회 관리자). 초대 코드가 없으면 생성
   */
  @PostMapping("/invite")
  public ResponseEntity<Object> getInvite() {
    return new ResponseEntity<>(ecclesiaService.getInvite(), HttpStatus.OK);
  }

  /**
   * 초대 코드 재발급 (기존 초대 링크는 즉시 무효)
   */
  @PostMapping("/invite/regenerate")
  public ResponseEntity<Object> regenerateInvite() {
    return new ResponseEntity<>(ecclesiaService.regenerateInvite(), HttpStatus.OK);
  }

  /**
   * 초대 가입 방식 설정 (autoApprove: 바로 가입 / 승인 후 가입)
   */
  @PatchMapping("/invite/settings")
  public ResponseEntity<Object> updateInviteSettings(
      @RequestBody EcclesiaInviteSettingsRequestDTO request) {
    return new ResponseEntity<>(ecclesiaService.updateInviteSettings(request.isAutoApprove()),
        HttpStatus.OK);
  }

  /**
   * 초대 코드로 교회 소개 조회 (비로그인 공개)
   */
  @GetMapping("/invite/info/{code}")
  public ResponseEntity<Object> getInviteInfo(@PathVariable("code") String code) {
    return new ResponseEntity<>(ecclesiaService.getInviteInfo(code), HttpStatus.OK);
  }

  /**
   * 초대 코드로 교회 가입
   */
  @PostMapping("/join/invite")
  public ResponseEntity<Object> joinByInvite(@RequestBody EcclesiaInviteJoinRequestDTO request) {
    return new ResponseEntity<>(ecclesiaService.joinByInvite(request.getCode()), HttpStatus.OK);
  }

  @PostMapping("/list/join-request")
  public ResponseEntity<Object> joinRequestList() {
    List<AccountEcclesiaHistoryDTO> result = ecclesiaService.getJoinRequestList();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

}
