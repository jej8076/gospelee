package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.EcclesiaStatusType;
import com.gospelee.api.repository.EcclesiaRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EcclesiaServiceImpl implements EcclesiaService {

  private final EcclesiaRepository ecclesiaRepository;

  public EcclesiaServiceImpl(EcclesiaRepository ecclesiaRepository) {
    this.ecclesiaRepository = ecclesiaRepository;
  }

  public List<EcclesiaResponseDTO> getEcclesiaAll() {
    List<EcclesiaResponseDTO> ecclesiaResponseProjections = ecclesiaRepository.findAllWithMasterName();
    return ecclesiaResponseProjections;
  }

  /**
   * <pre>
   * 교회 등록(요청)
   * ecclesia 테이블에 교회 등록 요청 상태로 입력하며 사용자 권한은 변경하지 않음
   * 교회 등록 승인 시에 사용자 권한(RoleType)을 담임목사(SENIOR_PASTER)로 변경
   * </pre>
   *
   * @param ecclesiaInsertDTO
   * @return
   */
  public Ecclesia saveEcclesia(EcclesiaInsertDTO ecclesiaInsertDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Ecclesia ecclesia = Ecclesia.builder()
        .name(ecclesiaInsertDTO.getName())
        .churchIdentificationNumber(ecclesiaInsertDTO.getChurchIdentificationNumber())
        .status(EcclesiaStatusType.APPROVAL.getName())
        // insert를 요청하는 인증된 사용자가 교회의 master account가 되도록 강제함
        .masterAccountUid(account.getUid())
        .build();

    return ecclesiaRepository.save(ecclesia);
  }


}


