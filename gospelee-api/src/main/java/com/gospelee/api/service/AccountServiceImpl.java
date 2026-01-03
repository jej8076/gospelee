package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDecideDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDetailDTO;
import com.gospelee.api.dto.account.AccountLeaveResponseDTO;
import com.gospelee.api.dto.account.TokenDTO;
import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.dto.kakao.UserMeResponse;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.entity.AccountMeta;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.AccountEcclesiaHistoryStatusType;
import com.gospelee.api.enums.Bearer;
import com.gospelee.api.enums.EcclesiaStatusType;
import com.gospelee.api.enums.RedisCacheNames;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.enums.SocialLoginPlatform;
import com.gospelee.api.enums.TokenHeaders;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.exception.AccountNotFoundException;
import com.gospelee.api.exception.EcclesiaException;
import com.gospelee.api.exception.KakaoResponseException;
import com.gospelee.api.exception.MissingRequiredValueException;
import com.gospelee.api.properties.AuthProperties;
import com.gospelee.api.repository.AccountEcclesiaHistoryRepository;
import com.gospelee.api.repository.jpa.account.AccountMetaRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.ecclesia.EcclesiaJpaRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import com.gospelee.api.utils.CamelCaseJsonUtils;
import com.gospelee.api.utils.SnakeCaseJsonUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClient;

/**
 * 계정 관련 비즈니스 로직을 처리하는 서비스 구현체 - 계정 조회, 생성, 업데이트 등의 기능을 제공 - JWT 토큰 기반 인증 처리 - 교회 정보와 연동된 계정 관리
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

  private final AuthProperties authProperties;
  private final AccountRepository accountRepository;
  private final AccountMetaRepository accountMetaRepository;
  private final AccountEcclesiaHistoryRepository accountEcclesiaHistoryRepository;
  private final EcclesiaJpaRepository ecclesiaJpaRepository;
  private final RedisCacheService redisCacheService;
  private final SnakeCaseJsonUtils snakeCaseJsonUtils;
  private final CamelCaseJsonUtils camelCaseJsonUtils;
  private final RestClient restClient;

  public AccountServiceImpl(AuthProperties authProperties, AccountRepository accountRepository,
      AccountMetaRepository accountMetaRepository,
      AccountEcclesiaHistoryRepository accountEcclesiaHistoryRepository,
      EcclesiaJpaRepository ecclesiaJpaRepository, RedisCacheService redisCacheService,
      RestClient.Builder restClient, SnakeCaseJsonUtils snakeCaseJsonUtils,
      CamelCaseJsonUtils camelCaseJsonUtils) {
    this.authProperties = authProperties;
    this.accountRepository = accountRepository;
    this.accountMetaRepository = accountMetaRepository;
    this.accountEcclesiaHistoryRepository = accountEcclesiaHistoryRepository;
    this.ecclesiaJpaRepository = ecclesiaJpaRepository;
    this.redisCacheService = redisCacheService;
    this.snakeCaseJsonUtils = snakeCaseJsonUtils;
    this.camelCaseJsonUtils = camelCaseJsonUtils;

    MediaType MEDIA_TYPE_FORM_UTF_8 = new MediaType(
        MediaType.APPLICATION_FORM_URLENCODED,
        java.nio.charset.StandardCharsets.UTF_8);

    this.restClient = restClient
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_FORM_UTF_8.toString())
        .baseUrl("https://kapi.kakao.com")
        .messageConverters(converters -> {
          converters.clear(); // 기존 기본 컨버터 제거 (선택)
          converters.add(snakeCaseJsonUtils.converter()); // 커스텀 컨버터 추가
        })
        .build();
  }

  /**
   * 모든 계정을 조회합니다.
   *
   * @return 전체 계정 목록
   */
  @Override
  public List<Account> getAccountAll() {
    log.debug("전체 계정 목록 조회 요청");
    return accountRepository.findAll();
  }

  /**
   * 교회 UID로 해당 교회의 계정 목록을 조회합니다.
   *
   * @param ecclesiaUid 교회 UID
   * @return 교회별 계정 목록
   */
  @Override
  public Optional<List<Account>> getAccountByEcclesiaUid(Long ecclesiaUid) {
    log.debug("교회별 계정 목록 조회 요청. ecclesiaUid: {}", ecclesiaUid);
    return accountRepository.findByEcclesiaUid(ecclesiaUid);
  }

  @Override
  public List<AccountEcclesiaHistoryDetailDTO> getAccountEcclesiaRequestList() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (account.getEcclesiaUid() == null) {
      throw new EcclesiaException("[ACCOUNT   ] not_affiliated_with_church accountUid:{}",
          account.getUid());
    }

    return accountEcclesiaHistoryRepository.findByAccountEcclesiaRequestByEcclesiaUid(
        account.getEcclesiaUid());
  }

  @Override
  public AccountEcclesiaHistoryDTO decideJoinRequest(
      AccountEcclesiaHistoryDecideDTO accountEcclesiaHistoryDecideDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (account.getEcclesiaUid() == null) {
      throw new EcclesiaException("[ACCOUNT   ] not_affiliated_with_church accountUid:{}",
          account.getUid());
    }

    AccountEcclesiaHistory findAccountEcclesiaHistory = accountEcclesiaHistoryRepository.findById(
        accountEcclesiaHistoryDecideDTO.getId());
    if (findAccountEcclesiaHistory.getEcclesiaUid() != account.getEcclesiaUid()) {
      // 요청에 대한 교회 정보가 현재 로그인 계정의 교회 정보와 다를 경우
      throw new EcclesiaException(
          "[ACCOUNT   ] church_information_mismatch accountUid:{} ecclesiaUid:{} requestEcclesiaUid:{}",
          account.getUid(), account.getEcclesiaUid(), findAccountEcclesiaHistory.getEcclesiaUid());
    }

    // 요청된 상태
    AccountEcclesiaHistoryStatusType requestedStatus = AccountEcclesiaHistoryStatusType.of(
        accountEcclesiaHistoryDecideDTO.getStatus());

    Optional<Account> requestedAccountOptional = accountRepository.findById(
        findAccountEcclesiaHistory.getAccountUid());
    if (requestedAccountOptional.isEmpty()) {
      throw new AccountNotFoundException("[ACCOUNT   ] not_found accountUid:{}", account.getUid());
    }

    // 승인의 경우에만 요청된 사용자의 ecclesiaUid를 부여함
    if (requestedStatus.name().equals(AccountEcclesiaHistoryStatusType.JOIN_APPROVAL.name())) {
      Account requestedAccount = requestedAccountOptional.get();
      requestedAccount.changeEcclesiaUid(findAccountEcclesiaHistory.getEcclesiaUid());
      accountRepository.save(requestedAccount);
    }

    AccountEcclesiaHistory accountEcclesiaHistory = AccountEcclesiaHistory.builder()
        // 요청한 사람의 계정
        .accountUid(findAccountEcclesiaHistory.getAccountUid())
        // 요청받은 교회
        .ecclesiaUid(findAccountEcclesiaHistory.getEcclesiaUid())
        // 요청된 상태
        .status(requestedStatus)
        .build();

    return AccountEcclesiaHistoryDTO.fromEntity(
        accountEcclesiaHistoryRepository.save(accountEcclesiaHistory));
  }

  @Override
  public AccountLeaveResponseDTO leaveAccount(AccountAuthDTO accountRequest) {
    Optional<Account> account = accountRepository.findById(accountRequest.getUid());
    if (account.isEmpty()) {
      throw new AccountNotFoundException("탈퇴할 계정이 존재하지 않습니다.");
    }
    Account getAccount = account.get();
    getAccount.changeLeaveYn(Yn.Y);
    accountRepository.save(getAccount);
    return AccountLeaveResponseDTO.builder()
        .uid(getAccount.getUid())
        .build();
  }

  /**
   * 전화번호로 계정을 조회합니다.
   *
   * @param phone 전화번호
   * @return 계정 정보
   */
  @Override
  public Optional<Account> getAccountByPhone(String phone) {
    log.debug("전화번호로 계정 조회 요청. phone: {}", phone);
    return accountRepository.findByPhone(phone);
  }

  /**
   * 이메일로 계정을 조회합니다.
   *
   * @param email 이메일 주소
   * @return 계정 정보
   */
  @Override
  public Optional<Account> getAccountByEmail(String email) {
    log.debug("이메일로 계정 조회 요청. email: {}", email);
    return accountRepository.findByEmail(email);
  }

  // ========== 계정 생성 및 업데이트 관련 메서드 ==========

  /**
   * JWT 페이로드를 기반으로 계정을 저장하거나 업데이트하고 인증 정보를 반환합니다.
   *
   * @param jwtPayload JWT 페이로드 정보
   * @param tokenDTO   TokenDTO
   * @return 인증된 계정 정보
   */
  @Override
  public Optional<AccountAuthDTO> saveAndGetAccount(JwtPayload jwtPayload, TokenDTO tokenDTO) {
    log.debug("계정 저장/업데이트 요청. email:{}, idToken:{}", jwtPayload.getEmail(),
        tokenDTO.getIdToken());

    Account account = findOrCreateAccount(jwtPayload, tokenDTO);
    return buildAccountAuthDTO(account, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
  }

  /**
   * 계정의 푸시 토큰을 저장합니다.
   *
   * @param uid       계정 UID
   * @param pushToken 푸시 토큰
   */
  @Override
  public void savePushToken(Long uid, String pushToken) {
    log.debug("푸시 토큰 저장 요청. uid: {}", uid);
    accountRepository.savePushToken(uid, pushToken, LocalDateTime.now());
  }

  // TODO 캐싱 관련 AOP로 전환 필요
  @Override
  public UserMeResponse getKakaoUserMe(String accessToken) {

    String cachedUserMe = redisCacheService.get(RedisCacheNames.USER_ME, accessToken);
    if (cachedUserMe != null) {
      return camelCaseJsonUtils.deserialization(cachedUserMe, UserMeResponse.class);
    }

    Optional<UserMeResponse> userMeClientResponse = restClient.post()
        .uri("/v2/user/me")
        .headers(headers -> {
          headers.add(TokenHeaders.AUTHORIZATION.getValue(),
              Bearer.BEARER_SPACE.getValue() + accessToken);
        })
        .exchange((request, response) -> {
          if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.bodyTo(UserMeResponse.class));
          } else {
            // 로그 찍고 Optional.empty() 반환
            String errorBody = response.bodyTo(String.class);
            log.error("kakao 요청 후 오류 응답 -> {}", errorBody);

            throw new KakaoResponseException("kakao 요청 후 오류 응답 -> %s", errorBody);
          }
        });

    if (userMeClientResponse.isEmpty()) {
      throw new KakaoResponseException("kakao 요청에 대한 응답이 없습니다");
    }

    UserMeResponse userMeResponse = userMeClientResponse.get();

    if (userMeResponse.getKakaoAccount() == null
        || userMeResponse.getKakaoAccount().getPhoneNumber() == null) {
      throw new MissingRequiredValueException("phone_number_is_empty kakaoId:%s accessToken:%s",
          userMeResponse.getId(), accessToken);
    }

    RedisCacheDTO cacheDTO = RedisCacheDTO.builder()
        .redisCacheNames(RedisCacheNames.USER_ME)
        .key(accessToken)
        .value(userMeResponse)
        .build();
    redisCacheService.put(cacheDTO);

    return userMeResponse;
  }

  /**
   * 슈퍼 유저 인증을 처리합니다.
   */
  public Optional<AccountAuthDTO> handleSuperUserAuthentication() {
    log.debug("슈퍼 유저 인증 처리");

    Account superAccount = accountRepository.findByEmail(authProperties.getSuperId())
        .orElseThrow(() -> new RuntimeException("슈퍼 계정을 찾을 수 없습니다."));

    Optional<Ecclesia> ecclesia = ecclesiaJpaRepository.findEcclesiasByMasterAccountUid(
        superAccount.getUid());

    AccountAuthDTO authDTO = AccountAuthDTO.builder()
        .uid(superAccount.getUid())
        .email(superAccount.getEmail())
        .name(superAccount.getName())
        .phone(superAccount.getPhone())
        .rrn(superAccount.getRrn())
        .role(RoleType.ADMIN)
        .ecclesiaUid(ecclesia.map(Ecclesia::getUid).orElse(null))
        .ecclesiaStatus(ecclesia.map(Ecclesia::getStatus).orElse(null))
        .build();

    return Optional.of(authDTO);
  }

  /**
   * 기존 계정을 찾거나 새로운 계정을 생성합니다.
   */
  private Account findOrCreateAccount(JwtPayload jwtPayload, TokenDTO tokenDTO) {
    return findAccountOrMetaByEmail(jwtPayload.getEmail())
        .map(existingAccount -> updateExistingAccount(existingAccount, tokenDTO))
        .orElseGet(() -> createNewAccount(jwtPayload, tokenDTO));
  }

  /**
   * 기존 계정의 ID 토큰을 업데이트합니다.
   */
  private Account updateExistingAccount(Account existingAccount, TokenDTO tokenDTO) {
    log.debug("기존 계정 ID 토큰 업데이트. uid: {}", existingAccount.getUid());

    return accountRepository.updateAccountIdTokenAndFindById(existingAccount.getUid(),
        tokenDTO.getIdToken());
  }

  /**
   * 새로운 계정을 생성합니다.
   */
  private Account createNewAccount(JwtPayload jwtPayload, TokenDTO tokenDTO) {
    log.debug("새로운 계정 생성. email:{}", jwtPayload.getEmail());

    Account newAccount = Account.builder()
        .name(jwtPayload.getNickname())
        .email(jwtPayload.getEmail())
        .role(RoleType.LAYMAN)
        .idToken(tokenDTO.getIdToken())
        .leaveYn(Yn.N)
        .build();

    Account savedAccount = accountRepository.save(newAccount);

    if (ObjectUtils.isEmpty(savedAccount)) {
      throw new RuntimeException("계정 저장에 실패했습니다.");
    }

    AccountMeta accountMeta = AccountMeta.builder()
        .accountUid(newAccount.getUid())
        .identifier(
            tokenDTO.getSocialLoginPlatform() == SocialLoginPlatform.APPLE ? jwtPayload.getSub()
                : null)
        .email(newAccount.getEmail())
        .insertTime(LocalDateTime.now())
        .build();

    accountMetaRepository.save(accountMeta);

    return savedAccount;
  }

  private Optional<Account> findAccountOrMetaByEmail(String email) {
    Optional<Account> accountOpt = accountRepository.findByEmail(email);
    if (accountOpt.isPresent()) {
      return accountOpt;
    }

    Optional<AccountMeta> metaOpt = accountMetaRepository.findByEmail(email);
    if (metaOpt.isPresent()) {
      AccountMeta meta = metaOpt.get();
      return accountRepository.findById(meta.getAccountUid());
    }

    return Optional.empty();
  }

  /**
   * 계정 정보를 기반으로 인증 DTO를 생성합니다.
   */
  private Optional<AccountAuthDTO> buildAccountAuthDTO(Account account, String accessToken,
      String refreshToken) {
    log.debug("계정 인증 DTO 생성. uid: {}", account.getUid());

    AccountAuthDTO.AccountAuthDTOBuilder authDTOBuilder = AccountAuthDTO.builder()
        .uid(account.getUid())
        .email(account.getEmail())
        .name(account.getName())
        .phone(account.getPhone())
        .rrn(account.getRrn())
        .role(account.getRole())
        .idToken(account.getIdToken())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .pushToken(account.getPushToken())
        .leaveYn(account.getLeaveYn());

    if (account.getEcclesiaUid() == null) {
      // 계정의 ecclesiaUid 정보가 없어도 ecclesia의 masterAccountUid로 로그인한 계정을 조회했을 때 결과가 있으면 해당 교회에 소속된 것으로 간주한다
      Optional<Ecclesia> ecclesia = ecclesiaJpaRepository.findEcclesiasByMasterAccountUid(
          account.getUid());
      authDTOBuilder
          .ecclesiaUid(ecclesia.map(Ecclesia::getUid).orElse(null))
          .ecclesiaStatus(ecclesia.map(Ecclesia::getStatus).orElse(null));
    } else {
      Optional<Ecclesia> ecclesia = ecclesiaJpaRepository.findById(account.getEcclesiaUid());

      authDTOBuilder
          .ecclesiaUid(account.getEcclesiaUid())
          .ecclesiaStatus(ecclesia.map(Ecclesia::getStatus).orElse(EcclesiaStatusType.NONE.name()));
    }

    return Optional.of(authDTOBuilder.build());
  }
}


