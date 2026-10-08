package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteInfoDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteJoinResultDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaJoinRequestStatusDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaUpdateDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaVerifyRequestDTO;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.entity.Ecclesia;
import java.util.List;

public interface EcclesiaService {

  List<EcclesiaResponseDTO> getEcclesiaList();

  List<EcclesiaResponseDTO> searchEcclesia(String text);

  Ecclesia getEcclesia(Long ecclesiaUid);

  Ecclesia getEcclesiaByAccountUid(Long accountUid);

  Ecclesia saveEcclesia(EcclesiaInsertDTO ecclesiaInsertDTO);

  EcclesiaResponseDTO updateEcclesia(EcclesiaUpdateDTO ecclesiaUpdateDTO);

  // 운영자(ADMIN) 전용: 교회 검증 완료/취소
  EcclesiaResponseDTO updateVerification(EcclesiaVerifyRequestDTO request);

  AccountEcclesiaHistory joinRequestEcclesia(Long ecclesiaUid);

  List<AccountEcclesiaHistoryDTO> getJoinRequestList();

  EcclesiaJoinRequestStatusDTO getMyJoinRequestStatus();

  void cancelJoinRequest();

  // 초대 링크 (교회 관리자)
  EcclesiaInviteDTO getInvite();

  EcclesiaInviteDTO regenerateInvite();

  EcclesiaInviteDTO updateInviteSettings(boolean autoApprove);

  // 초대 코드로 교회 소개 조회 (비로그인 공개)
  EcclesiaInviteInfoDTO getInviteInfo(String code);

  // 초대 코드로 가입 (승인 필요 설정이면 요청, 바로 가입 설정이면 즉시 가입)
  EcclesiaInviteJoinResultDTO joinByInvite(String code);
}
