package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaJoinRequestStatusDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaUpdateDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.AccountEcclesiaHistoryStatusType;
import com.gospelee.api.enums.EcclesiaStatusType;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.exception.AccountNotFoundException;
import com.gospelee.api.exception.EcclesiaException;
import com.gospelee.api.repository.AccountEcclesiaHistoryRepository;
import com.gospelee.api.repository.EcclesiaRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EcclesiaServiceImpl implements EcclesiaService {

  private final EcclesiaRepository ecclesiaRepository;
  private final AccountEcclesiaHistoryRepository accountEcclesiaHistoryRepository;
  private final AuthorizationService authorizationService;
  private final AccountRepository accountRepository;

  @Override
  public List<EcclesiaResponseDTO> getEcclesiaList() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!RoleType.ADMIN.equals(account.getRole())) {
      throw new AccessDeniedException("접근할 권한이 없습니다.");
    }
    return ecclesiaRepository.findAllWithMasterName();
  }

  @Override
  public List<EcclesiaResponseDTO> searchEcclesia(String text) {
    return ecclesiaRepository.searchEcclesia(text);
  }

  @Override
  public Ecclesia getEcclesia(Long ecclesiaUid) {
    if (ecclesiaUid == 0) {
      return null;
    }
    return ecclesiaRepository.findEcclesiasByUid(ecclesiaUid)
        .orElseThrow(
            () -> new IllegalArgumentException("해당 UID를 가진 Ecclesia를 찾을 수 없습니다: " + ecclesiaUid));
  }

  @Override
  public Ecclesia getEcclesiaByAccountUid(Long accountUid) {
    return ecclesiaRepository.findEcclesiasByMasterAccountUid(accountUid)
        .orElseThrow(
            () -> new IllegalArgumentException(
                "해당 accountUid를 가진 Ecclesia를 찾을 수 없습니다: " + accountUid));
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
  @Override
  public Ecclesia saveEcclesia(EcclesiaInsertDTO ecclesiaInsertDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Ecclesia ecclesia = Ecclesia.builder()
        .name(ecclesiaInsertDTO.getName())
        .churchIdentificationNumber(ecclesiaInsertDTO.getChurchIdentificationNumber())
        .telephone(ecclesiaInsertDTO.getTelephone())
        .status(EcclesiaStatusType.REQUEST.getName())
        // insert를 요청하는 인증된 사용자가 교회의 master account가 되도록 강제함
        .masterAccountUid(account.getUid())
        .build();

    Optional<Account> findAccount = accountRepository.findById(account.getUid());
    if (findAccount.isEmpty()) {
      throw new AccountNotFoundException("계정이 존재하지 않습니다. accountUid:{}",
          findAccount.get().getUid());
    }

    Ecclesia saveEcclesia = ecclesiaRepository.save(ecclesia);
    if (saveEcclesia.getUid() <= 0) {
      throw new EcclesiaException("교회 등록 요청에 실패하였습니다. requestChurchName:{} accountUid:{}",
          ecclesiaInsertDTO.getName(), account.getUid());
    }

    // 등록 요청자의 교회 소속을 결정
    Account acc = findAccount.get();
    acc.changeEcclesiaUid(ecclesia.getUid());
    accountRepository.save(acc);

    return saveEcclesia;
  }

  @Override
  @Transactional
  public EcclesiaResponseDTO updateEcclesia(EcclesiaUpdateDTO ecclesiaUpdateDTO) {

    // Ecclesia 조회, 없으면 예외 발생
    Ecclesia ecclesia = ecclesiaRepository.findById(ecclesiaUpdateDTO.getEcclesiaUid()).orElseThrow(
        () -> new EntityNotFoundException(
            "Ecclesia not found with id: " + ecclesiaUpdateDTO.getEcclesiaUid()));

    AccountAuthDTO accountAuth = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!authorizationService.canUpdateEcclesiaStatus(accountAuth, ecclesia)) {
      throw new AccessDeniedException("접근할 권한이 없습니다.");
    }

    EcclesiaStatusType requestType = null;
    if (ecclesiaUpdateDTO.getStatus() != null) {
      requestType = EcclesiaStatusType.fromName(ecclesiaUpdateDTO.getStatus());
      ecclesia.changeStatus(requestType);
    }

    if (ecclesiaUpdateDTO.getSeniorPastorName() != null) {
      ecclesia.changeSeniorPastorName(ecclesiaUpdateDTO.getSeniorPastorName());
    }

    if (ecclesiaUpdateDTO.getChurchAddress() != null) {
      ecclesia.changeChurchAddress(ecclesiaUpdateDTO.getChurchAddress());
    }

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

  @Override
  public AccountEcclesiaHistory joinRequestEcclesia(Long ecclesiaUid) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    if (account.getEcclesiaUid() != null) {
      throw new EcclesiaException("이미 교회에 등록 요청 되었거나 소속되었습니다.");
    }

    // 한 번에 한 교회에만 가입 요청 가능 (가장 최근 이력이 요청 상태이면 대기 중)
    AccountEcclesiaHistory latest = accountEcclesiaHistoryRepository.findLatestByAccountUid(
        account.getUid());
    if (latest != null && latest.getStatus() == AccountEcclesiaHistoryStatusType.JOIN_REQUEST) {
      throw new EcclesiaException("이미 가입 요청 중인 교회가 있습니다. 요청을 취소한 후 다시 시도해주세요.");
    }

    AccountEcclesiaHistory accountEcclesiaHistory = AccountEcclesiaHistory.builder()
        .accountUid(account.getUid())
        .ecclesiaUid(ecclesiaUid)
        .status(AccountEcclesiaHistoryStatusType.JOIN_REQUEST)
        .insertTime(LocalDateTime.now())
        .build();

    return accountEcclesiaHistoryRepository.save(accountEcclesiaHistory);
  }

  @Override
  public List<AccountEcclesiaHistoryDTO> getJoinRequestList() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = account.getEcclesiaUid();
    if (ecclesiaUid == null) {
      throw new EcclesiaException("소속된 교회 정보가 없습니다.");
    }

    return accountEcclesiaHistoryRepository.findByStatusAndEcclesiaId(account.getEcclesiaUid());
  }

  /**
   * 내 교회 가입 요청 상태 조회
   * 가장 최근 이력이 요청이면 PENDING, 반려면 REJECTED, 그 외에는 NONE
   */
  @Override
  public EcclesiaJoinRequestStatusDTO getMyJoinRequestStatus() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    AccountEcclesiaHistory latest = accountEcclesiaHistoryRepository.findLatestByAccountUid(
        account.getUid());
    if (latest == null) {
      return EcclesiaJoinRequestStatusDTO.none();
    }

    String status;
    if (latest.getStatus() == AccountEcclesiaHistoryStatusType.JOIN_REQUEST) {
      status = "PENDING";
    } else if (latest.getStatus() == AccountEcclesiaHistoryStatusType.JOIN_REJECT) {
      status = "REJECTED";
    } else {
      return EcclesiaJoinRequestStatusDTO.none();
    }

    String ecclesiaName = ecclesiaRepository.findById(latest.getEcclesiaUid())
        .map(Ecclesia::getName)
        .orElse(null);

    return EcclesiaJoinRequestStatusDTO.builder()
        .ecclesiaUid(latest.getEcclesiaUid())
        .ecclesiaName(ecclesiaName)
        .status(status)
        .build();
  }

  /**
   * 가입 요청 취소 (반려된 요청의 확인 처리에도 사용)
   * 이력은 삭제하지 않고 LEAVE 이력을 추가하여 요청을 종료함
   */
  @Override
  public void cancelJoinRequest() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    AccountEcclesiaHistory latest = accountEcclesiaHistoryRepository.findLatestByAccountUid(
        account.getUid());
    if (latest == null
        || (latest.getStatus() != AccountEcclesiaHistoryStatusType.JOIN_REQUEST
        && latest.getStatus() != AccountEcclesiaHistoryStatusType.JOIN_REJECT)) {
      throw new EcclesiaException("취소할 가입 요청이 없습니다.");
    }

    accountEcclesiaHistoryRepository.save(AccountEcclesiaHistory.builder()
        .accountUid(account.getUid())
        .ecclesiaUid(latest.getEcclesiaUid())
        .status(AccountEcclesiaHistoryStatusType.LEAVE)
        .insertTime(LocalDateTime.now())
        .build());
  }
}
