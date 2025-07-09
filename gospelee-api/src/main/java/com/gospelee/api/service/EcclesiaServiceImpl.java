package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaUpdateDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.EcclesiaStatusType;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.AccountRepository;
import com.gospelee.api.repository.ecclesia.EcclesiaRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class EcclesiaServiceImpl implements EcclesiaService {

  private final EcclesiaRepository ecclesiaRepository;
  private final AuthorizationService authorizationService;
  private final AccountRepository accountRepository;

  public EcclesiaServiceImpl(EcclesiaRepository ecclesiaRepository,
      AuthorizationService authorizationService, AccountRepository accountRepository) {
    this.ecclesiaRepository = ecclesiaRepository;
    this.authorizationService = authorizationService;
    this.accountRepository = accountRepository;
  }

  public List<EcclesiaResponseDTO> getEcclesiaAll() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!RoleType.ADMIN.equals(account.getRole())) {
      throw new AccessDeniedException("접근할 권한이 없습니다.");
    }
    return ecclesiaRepository.findAllWithMasterName();
  }

  public Ecclesia getEcclesia(Long ecclesiaUid) {
    return ecclesiaRepository.findEcclesiasByUid(ecclesiaUid)
        .orElseThrow(
            () -> new IllegalArgumentException("해당 UID를 가진 Ecclesia를 찾을 수 없습니다: " + ecclesiaUid));
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
        .status(EcclesiaStatusType.REQUEST.getName())
        // insert를 요청하는 인증된 사용자가 교회의 master account가 되도록 강제함
        .masterAccountUid(account.getUid())
        .build();

    return ecclesiaRepository.save(ecclesia);
  }

  @Transactional
  public EcclesiaResponseDTO updateEcclesia(long uid, EcclesiaUpdateDTO ecclesiaUpdateDTO) {

    // Ecclesia 조회, 없으면 예외 발생
    Ecclesia ecclesia = ecclesiaRepository.findById(uid).orElseThrow(
        () -> new EntityNotFoundException("Ecclesia not found with id: " + uid));

    AccountAuthDTO accountAuth = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!authorizationService.canUpdateEcclesiaStatus(accountAuth, ecclesia)) {
      throw new AccessDeniedException("접근할 권한이 없습니다.");
    }

    EcclesiaStatusType requestType = EcclesiaStatusType.fromName(ecclesiaUpdateDTO.getStatus());
    ecclesia.changeStatus(requestType);

    Ecclesia ecc = ecclesiaRepository.save(ecclesia);

    // 교회 masterAccountUid 계정
    Account account = accountRepository.findById(ecclesia.getMasterAccountUid()).get();

    // 교회 masterAccountUid 계정의 권한 변경 -> SENIOR_PASTOR
    if (EcclesiaStatusType.APPROVAL == requestType) {
      account.changeRole(RoleType.SENIOR_PASTOR);
      accountRepository.save(account);
    }

    // 교회 masterAccountUid 계정의 권한 변경 -> LAYMAN
    if (EcclesiaStatusType.REJECT == requestType || EcclesiaStatusType.REQUEST == requestType) {
      account.changeRole(RoleType.LAYMAN);
      accountRepository.save(account);
    }

    return EcclesiaResponseDTO.fromEntity(ecc);
  }


}


