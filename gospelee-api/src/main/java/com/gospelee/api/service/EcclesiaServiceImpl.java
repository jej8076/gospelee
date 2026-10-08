package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteInfoDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteJoinResultDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaVerifyRequestDTO;
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
import com.gospelee.api.utils.ContactPhoneUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
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
  private final EcclesiaPushNotifier ecclesiaPushNotifier;
  private final SlackNotifier slackNotifier;

  // 헷갈리기 쉬운 문자(0/O, 1/l/I)를 제외한 초대 코드 문자셋
  private static final String INVITE_CODE_CHARS = "abcdefghjkmnpqrstuvwxyz23456789";
  private static final int INVITE_CODE_LENGTH = 12;
  private static final SecureRandom RANDOM = new SecureRandom();

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
    // 다른 교회의 정보(연락처 등)가 노출되지 않도록 내 교회 또는 운영자만 조회 가능
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!RoleType.ADMIN.equals(account.getRole())
        && !ecclesiaUid.equals(account.getEcclesiaUid())) {
      throw new AccessDeniedException("접근할 권한이 없습니다.");
    }
    return ecclesiaRepository.findEcclesiasByUid(ecclesiaUid)
        .orElseThrow(
            () -> new IllegalArgumentException("해당 UID를 가진 Ecclesia를 찾을 수 없습니다: " + ecclesiaUid));
  }

  @Override
  public Ecclesia getEcclesiaByAccountUid(Long accountUid) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!RoleType.ADMIN.equals(account.getRole()) && !accountUid.equals(account.getUid())) {
      throw new AccessDeniedException("접근할 권한이 없습니다.");
    }
    return ecclesiaRepository.findEcclesiasByMasterAccountUid(accountUid)
        .orElseThrow(
            () -> new IllegalArgumentException(
                "해당 accountUid를 가진 Ecclesia를 찾을 수 없습니다: " + accountUid));
  }

  /**
   * <pre>
   * 교회 등록
   * 진입 장벽을 낮추기 위해 신청 즉시 승인(APL)하고 신청자를 담임목사(SENIOR_PASTOR)로 지정한다.
   * 운영자가 전화 등으로 검증하기 전까지(verified_yn = N)는 성도 수, 저장 용량, 검색 노출이 제한되고
   * 신청 후 14일 안에 검증되지 않으면 일부 기능(초대, 공지)이 제한된다.
   * </pre>
   *
   * @param ecclesiaInsertDTO
   * @return
   */
  @Override
  @Transactional
  public Ecclesia saveEcclesia(EcclesiaInsertDTO ecclesiaInsertDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    if (ecclesiaInsertDTO.getName() == null || ecclesiaInsertDTO.getName().isBlank()) {
      throw new EcclesiaException("교회 이름을 입력해주세요.");
    }

    // 운영자가 이 번호로 전화해서 교회 관리자가 맞는지 검증하므로 연락 가능한 번호가 필수
    String telephone = ContactPhoneUtils.normalizeOrNull(ecclesiaInsertDTO.getTelephone());
    if (telephone == null) {
      throw new EcclesiaException("연락 가능한 전화번호를 정확히 입력해주세요.");
    }

    // 한 계정은 한 교회에만 소속/등록할 수 있다 (여러 교회 반복 등록 방지)
    if (account.getEcclesiaUid() != null
        || ecclesiaRepository.findEcclesiasByMasterAccountUid(account.getUid()).isPresent()) {
      throw new EcclesiaException("이미 교회에 소속되었거나 등록한 교회가 있습니다.");
    }
    AccountEcclesiaHistory latest = accountEcclesiaHistoryRepository.findLatestByAccountUid(
        account.getUid());
    if (latest != null && latest.getStatus() == AccountEcclesiaHistoryStatusType.JOIN_REQUEST) {
      throw new EcclesiaException("가입 요청 중인 교회가 있습니다. 요청을 취소한 후 등록해주세요.");
    }

    Account findAccount = accountRepository.findById(account.getUid())
        .orElseThrow(() -> new AccountNotFoundException("계정이 존재하지 않습니다. accountUid:{}",
            account.getUid()));

    Ecclesia ecclesia = Ecclesia.builder()
        .name(ecclesiaInsertDTO.getName().trim())
        .churchIdentificationNumber(ecclesiaInsertDTO.getChurchIdentificationNumber())
        .telephone(telephone)
        .status(EcclesiaStatusType.APPROVAL.getName())
        // insert를 요청하는 인증된 사용자가 교회의 master account가 되도록 강제함
        .masterAccountUid(account.getUid())
        .build();

    Ecclesia saveEcclesia = ecclesiaRepository.save(ecclesia);
    if (saveEcclesia.getUid() <= 0) {
      throw new EcclesiaException("교회 등록에 실패하였습니다. requestChurchName:{} accountUid:{}",
          ecclesiaInsertDTO.getName(), account.getUid());
    }

    // 등록자의 교회 소속과 담임목사 권한을 즉시 부여
    findAccount.changeEcclesiaUid(saveEcclesia.getUid());
    findAccount.changeRole(RoleType.SENIOR_PASTOR);
    accountRepository.save(findAccount);

    // 운영자가 전화로 검증할 수 있도록 Slack으로 알림 (커밋 후 비동기 전송)
    slackNotifier.notifyChurchRegistered(saveEcclesia, findAccount.getName(),
        findAccount.getPhone());

    return saveEcclesia;
  }

  @Override
  @Transactional
  public EcclesiaResponseDTO updateVerification(EcclesiaVerifyRequestDTO request) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!RoleType.ADMIN.equals(account.getRole())) {
      throw new AccessDeniedException("접근할 권한이 없습니다.");
    }
    if (request.getEcclesiaUid() == null || request.getVerified() == null) {
      throw new EcclesiaException("교회와 검증 여부를 입력해주세요.");
    }

    Ecclesia ecclesia = ecclesiaRepository.findById(request.getEcclesiaUid())
        .orElseThrow(() -> new EntityNotFoundException(
            "Ecclesia not found with id: " + request.getEcclesiaUid()));
    ecclesia.changeVerified(request.getVerified());

    return EcclesiaResponseDTO.fromEntity(ecclesiaRepository.save(ecclesia));
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

    // 교회 상태(승인/반려) 변경은 운영자(ADMIN)만 가능. 교회 대표는 이름/주소만 수정할 수 있다.
    if (ecclesiaUpdateDTO.getStatus() != null && !RoleType.ADMIN.equals(accountAuth.getRole())) {
      throw new AccessDeniedException("교회 상태를 변경할 권한이 없습니다.");
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

    assertCanRequestJoin(account);

    // 검색으로 가입 요청할 수 있는 교회는 승인되고 운영자 검증이 끝난 교회뿐이다
    // (검증 전 교회는 초대 링크로만 가입할 수 있다)
    Ecclesia target = ecclesiaRepository.findById(ecclesiaUid)
        .orElseThrow(() -> new EcclesiaException("교회 정보를 찾을 수 없습니다."));
    if (!EcclesiaStatusType.APPROVAL.getName().equals(target.getStatus()) || !target.isVerified()) {
      throw new EcclesiaException("초대 링크로만 가입할 수 있는 교회입니다.");
    }

    AccountEcclesiaHistory accountEcclesiaHistory = AccountEcclesiaHistory.builder()
        .accountUid(account.getUid())
        .ecclesiaUid(ecclesiaUid)
        .status(AccountEcclesiaHistoryStatusType.JOIN_REQUEST)
        .insertTime(LocalDateTime.now())
        .build();

    AccountEcclesiaHistory saved = accountEcclesiaHistoryRepository.save(accountEcclesiaHistory);

    // 교회 관리자에게 새 가입 요청 알림
    ecclesiaPushNotifier.notifyJoinRequested(target, account.getName());
    return saved;
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

  /**
   * 가입 요청 가능 여부 검사
   * 이미 소속(또는 교회 등록 신청)되었거나, 가입 요청 중인 교회가 있으면 불가 (한 번에 한 교회만 요청)
   */
  private void assertCanRequestJoin(AccountAuthDTO account) {
    if (account.getEcclesiaUid() != null) {
      throw new EcclesiaException("이미 교회에 등록 요청 되었거나 소속되었습니다.");
    }

    AccountEcclesiaHistory latest = accountEcclesiaHistoryRepository.findLatestByAccountUid(
        account.getUid());
    if (latest != null && latest.getStatus() == AccountEcclesiaHistoryStatusType.JOIN_REQUEST) {
      throw new EcclesiaException("이미 가입 요청 중인 교회가 있습니다. 요청을 취소한 후 다시 시도해주세요.");
    }
  }

  /**
   * 로그인 사용자가 관리하는 교회 조회 (승인된 교회의 관리자만 가능)
   */
  private Ecclesia getManagedEcclesia(AccountAuthDTO account) {
    if (account.getEcclesiaUid() == null) {
      throw new EcclesiaException("소속된 교회 정보가 없습니다.");
    }
    Ecclesia ecclesia = ecclesiaRepository.findById(account.getEcclesiaUid())
        .orElseThrow(() -> new EcclesiaException("교회 정보를 찾을 수 없습니다."));
    // 교회 대표(마스터)뿐 아니라 같은 교회의 담임목사/교역자/관리자도 초대 링크를 관리할 수 있다
    // (가입 요청 승인/반려 권한과 동일한 기준, 소속 교회는 로그인 계정의 교회로 한정됨)
    if (!authorizationService.canUpdateEcclesiaStatus(account, ecclesia)
        && !EcclesiaPushNotifier.isManagerRole(account.getRole())) {
      throw new AccessDeniedException("접근할 권한이 없습니다.");
    }
    if (!EcclesiaStatusType.APPROVAL.getName().equals(ecclesia.getStatus())) {
      throw new EcclesiaException("교회 등록이 승인된 후에 초대 링크를 만들 수 있습니다.");
    }
    return ecclesia;
  }

  private String generateInviteCode() {
    for (int attempt = 0; attempt < 10; attempt++) {
      StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
      for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
        sb.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
      }
      String code = sb.toString();
      if (!ecclesiaRepository.existsByInviteCode(code)) {
        return code;
      }
    }
    throw new EcclesiaException("초대 코드 생성에 실패했습니다. 다시 시도해주세요.");
  }

  private EcclesiaInviteDTO toInviteDTO(Ecclesia ecclesia) {
    return EcclesiaInviteDTO.builder()
        .inviteCode(ecclesia.getInviteCode())
        .autoApprove(ecclesia.isInviteAutoApprove())
        .build();
  }

  @Override
  @Transactional
  public EcclesiaInviteDTO getInvite() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Ecclesia ecclesia = getManagedEcclesia(account);

    // 코드가 없으면 최초 조회 시 생성
    if (ecclesia.getInviteCode() == null || ecclesia.getInviteCode().isBlank()) {
      ecclesia.changeInviteCode(generateInviteCode());
      ecclesia = ecclesiaRepository.save(ecclesia);
    }
    return toInviteDTO(ecclesia);
  }

  @Override
  @Transactional
  public EcclesiaInviteDTO regenerateInvite() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Ecclesia ecclesia = getManagedEcclesia(account);

    ecclesia.changeInviteCode(generateInviteCode());
    return toInviteDTO(ecclesiaRepository.save(ecclesia));
  }

  @Override
  @Transactional
  public EcclesiaInviteDTO updateInviteSettings(boolean autoApprove) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Ecclesia ecclesia = getManagedEcclesia(account);

    ecclesia.changeInviteAutoApprove(autoApprove);
    return toInviteDTO(ecclesiaRepository.save(ecclesia));
  }

  /**
   * 검증 전 교회는 성도 수 한도(30명)까지만 받을 수 있다
   */
  private void assertHasRoomForMember(Ecclesia ecclesia) {
    long memberCount = accountRepository.countByEcclesiaUid(ecclesia.getUid());
    if (!ecclesia.hasRoomForMember(memberCount)) {
      throw new EcclesiaException(
          "교회 성도 수 한도(" + Ecclesia.UNVERIFIED_MAX_MEMBERS + "명)에 도달했습니다. 교회에 문의해주세요.");
    }
  }

  /**
   * 초대 코드에 해당하는 승인된 교회 조회
   */
  private Ecclesia findInvitableEcclesia(String code) {
    String cleanCode = code == null ? "" : code.trim();
    if (cleanCode.isEmpty()) {
      throw new EcclesiaException("유효하지 않은 초대 링크입니다.");
    }
    Ecclesia ecclesia = ecclesiaRepository.findByInviteCode(cleanCode)
        .orElseThrow(() -> new EcclesiaException("유효하지 않은 초대 링크입니다."));
    if (!EcclesiaStatusType.APPROVAL.getName().equals(ecclesia.getStatus())) {
      throw new EcclesiaException("아직 승인되지 않은 교회입니다.");
    }
    if (ecclesia.isVerificationExpired()) {
      throw new EcclesiaException("교회 검증 기한이 지나 초대 링크를 사용할 수 없습니다. 교회에 문의해주세요.");
    }
    return ecclesia;
  }

  @Override
  public EcclesiaInviteInfoDTO getInviteInfo(String code) {
    Ecclesia ecclesia = findInvitableEcclesia(code);
    return EcclesiaInviteInfoDTO.builder()
        .name(ecclesia.getName())
        .seniorPastorName(ecclesia.getSeniorPastorName())
        .churchAddress(ecclesia.getChurchAddress())
        .memberCount(accountRepository.countByEcclesiaUid(ecclesia.getUid()))
        .build();
  }

  @Override
  @Transactional
  public EcclesiaInviteJoinResultDTO joinByInvite(String code) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Ecclesia ecclesia = findInvitableEcclesia(code);
    assertCanRequestJoin(account);
    assertHasRoomForMember(ecclesia);

    if (ecclesia.isInviteAutoApprove()) {
      // 바로 가입: 승인 이력을 남기고 소속 교회를 즉시 지정
      Account findAccount = accountRepository.findById(account.getUid())
          .orElseThrow(() -> new AccountNotFoundException("계정이 존재하지 않습니다. accountUid:{}",
              account.getUid()));
      accountEcclesiaHistoryRepository.save(AccountEcclesiaHistory.builder()
          .accountUid(account.getUid())
          .ecclesiaUid(ecclesia.getUid())
          .status(AccountEcclesiaHistoryStatusType.INVITE_APPROVAL)
          .insertTime(LocalDateTime.now())
          .build());
      findAccount.changeEcclesiaUid(ecclesia.getUid());
      accountRepository.save(findAccount);

      return EcclesiaInviteJoinResultDTO.builder()
          .ecclesiaUid(ecclesia.getUid())
          .ecclesiaName(ecclesia.getName())
          .status("JOINED")
          .build();
    }

    // 승인 필요: 일반 가입 요청과 동일하게 처리 (관리자가 승인/반려)
    accountEcclesiaHistoryRepository.save(AccountEcclesiaHistory.builder()
        .accountUid(account.getUid())
        .ecclesiaUid(ecclesia.getUid())
        .status(AccountEcclesiaHistoryStatusType.JOIN_REQUEST)
        .insertTime(LocalDateTime.now())
        .build());

    // 교회 관리자에게 새 가입 요청 알림
    ecclesiaPushNotifier.notifyJoinRequested(ecclesia, account.getName());

    return EcclesiaInviteJoinResultDTO.builder()
        .ecclesiaUid(ecclesia.getUid())
        .ecclesiaName(ecclesia.getName())
        .status("PENDING")
        .build();
  }
}
