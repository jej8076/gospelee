package com.gospelee.api.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.account.AccountDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDecideDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDetailDTO;
import com.gospelee.api.dto.account.PushTokenDTO;
import com.gospelee.api.dto.common.DataResponseDTO;
import com.gospelee.api.dto.common.NonceRequestDTO;
import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.dto.common.ResponseDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.QrLogin;
import com.gospelee.api.enums.DeepLinkRouterPath;
import com.gospelee.api.enums.EcclesiaStatusType;
import com.gospelee.api.enums.ErrorResponseType;
import com.gospelee.api.enums.PushNotificationDataType;
import com.gospelee.api.enums.RedisCacheName;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.FirebaseService;
import com.gospelee.api.service.QrloginService;
import com.gospelee.api.service.RedisCacheService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 계정 관련 API를 처리하는 컨트롤러 - 계정 조회, 인증, QR 로그인, 푸시 토큰 관리 등의 기능을 제공 test
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/account")
public class AccountController {

  private final AccountService accountService;
  private final QrloginService qrloginService;
  private final FirebaseService firebaseService;
  private final RedisCacheService redisCacheService;

  // ========== 계정 조회 관련 API ==========

  /**
   * 현재 로그인한 사용자의 정보를 조회합니다.
   *
   * @param account 인증된 사용자 정보
   * @return 사용자 정보
   */
  @PostMapping
  public ResponseEntity<Object> getCurrentUser(@AuthenticationPrincipal AccountAuthDTO account) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", account)
    );
  }

  /**
   * 모든 계정 목록을 조회합니다.
   *
   * @return 전체 계정 목록
   */
  @PostMapping("/all")
  public ResponseEntity<Object> getAllAccounts() {
    List<Account> accounts = accountService.getAccountAll();
    return ResponseEntity.ok(accounts);
  }

  @PostMapping("/nonce/put")
  public ResponseEntity<Object> putNonce(@RequestBody NonceRequestDTO nonceRequestDTO) {
    RedisCacheDTO redisCacheDTO = RedisCacheDTO.builder()
        .redisCacheName(RedisCacheName.NONCE)
        .key(nonceRequestDTO.getAnonymousId())
        .value(nonceRequestDTO.getNonce())
        .build();
    String nonce = redisCacheService.put(redisCacheDTO);
    return ResponseEntity.ok(DataResponseDTO.of("100", "성공", nonce));
  }

  /**
   * 교회별 계정 목록을 조회합니다. - ADMIN 권한: 전체 목록 조회 - 일반 사용자: 본인 교회의 계정 목록만 조회
   *
   * @param account 인증된 사용자 정보
   * @return 계정 목록
   */
  @PostMapping("/getAccount/list")
  public ResponseEntity<Object> getAccountsByEcclesia(
      @AuthenticationPrincipal AccountAuthDTO account) {

    if (isAdminUser(account)) {
      return handleAdminAccountRequest();
    }

    return handleUserAccountRequest(account);
  }

  /**
   * 로그인된 계정의 교회 정보로 교회 참여 요청된 목록 조회
   *
   * @return
   */
  @PostMapping("/ecclesia/join/request/list")
  public ResponseEntity<Object> getAccountEcclesiaRequestList() {
    List<AccountEcclesiaHistoryDetailDTO> accountEcclesiaHistoryDetailList = accountService.getAccountEcclesiaRequestList();
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", accountEcclesiaHistoryDetailList)
    );
  }

  /**
   * 교회에 참여 요청된 데이터를 변경한다
   *
   * @return
   */
  @PostMapping("/ecclesia/join/request/decide")
  public ResponseEntity<Object> dicideJoinRequest(
      @RequestBody AccountEcclesiaHistoryDecideDTO accountEcclesiaHistoryDecideDTO) {
    AccountEcclesiaHistoryDTO accountEcclesiaHistory = accountService.decideJoinRequest(
        accountEcclesiaHistoryDecideDTO);
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", accountEcclesiaHistory)
    );
  }

  /**
   * 계정 인증 상태를 확인합니다.
   * TODO: ControllerAdvice를 사용하여 예외 처리 개선 필요
   *
   * @param account 인증된 사용자 정보
   * @return 인증 결과
   */
  @PostMapping("/auth/validate")
  public ResponseEntity<ResponseDTO> validateAccount(
      @AuthenticationPrincipal AccountAuthDTO account) {
    ErrorResponseType errorResponse = validateAccountStatus(account);

    if (errorResponse != null) {
      return ResponseEntity.ok(
          DataResponseDTO.of(errorResponse.code(), errorResponse.message(), account)
      );
    }

    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", account)
    );
  }

  // ========== 푸시 토큰 관리 API ==========

  /**
   * 사용자의 푸시 토큰을 업데이트합니다.
   *
   * @param account      인증된 사용자 정보
   * @param pushTokenDTO 푸시 토큰 정보
   * @return 업데이트 결과
   */
  @PatchMapping("/auth/success")
  public ResponseEntity<Object> updatePushToken(
      @AuthenticationPrincipal AccountAuthDTO account,
      @RequestBody PushTokenDTO pushTokenDTO) {

    accountService.savePushToken(account.getUid(), pushTokenDTO.getPushToken());
    log.info("[PUSHTOKEN ] update_success accountUid:{} pushToken:{}", account.getUid(),
        pushTokenDTO.getPushToken());

    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", account)
    );
  }

  // ========== QR 로그인 관련 API ==========

  /**
   * QR 로그인을 시작합니다. 1. QR 로그인 코드 생성 및 DB 저장 2. 앱에 푸시 메시지 전송
   *
   * @param qrRequest QR 로그인 요청 정보
   * @return QR 로그인 정보
   */
  @PostMapping("/qr/enter")
  public ResponseEntity<Object> initiateQrLogin(@RequestBody AccountDTO.QrRequest qrRequest) {
    QrLogin qrLogin = qrloginService.saveQrlogin(qrRequest.getEmail());

    Account account = findAccountByEmail(qrRequest.getEmail());

    if (hasPushToken(account)) {
      sendQrLoginNotification(account, qrLogin.getCode());
    }

    return ResponseEntity.ok(qrLogin);
  }

  /**
   * QR 코드 스캔 시 인증을 처리합니다. 앱에서 QR 코드를 스캔할 때 호출되며, 인증 성공 시 코드에 토큰을 저장합니다.
   *
   * @param account 인증된 사용자 정보
   * @param code    QR 로그인 코드
   * @return 인증 결과
   */
  @PostMapping("/qr/req/{code}")
  public ResponseEntity<Object> authenticateQrLogin(
      @AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable(value = "code") String code) {

    boolean isSuccess = qrloginService.updateQrlogin(account, code);
    HttpStatus status = isSuccess ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

    return new ResponseEntity<>("", status);
  }

  /**
   * QR 로그인 상태를 확인합니다. 프론트엔드에서 주기적으로 호출하여 로그인 완료 여부를 확인합니다.
   * TODO: 웹소켓으로 대체 예정
   *
   * @param qrCheckRequest QR 확인 요청 정보
   * @return QR 로그인 상태
   */
  @PostMapping("/qr/check")
  public ResponseEntity<Object> checkQrLoginStatus(
      @RequestBody AccountDTO.QrCheckRequest qrCheckRequest) {

    QrLogin qrLogin = qrloginService.getQrLogin(
        qrCheckRequest.getEmail(),
        qrCheckRequest.getCode()
    );

    return ResponseEntity.ok(qrLogin);
  }

  // ========== 테스트 API ==========

  /**
   * 푸시 알림 테스트용 API
   *
   * @param account 인증된 사용자 정보
   * @return 알림 전송 결과
   * @throws FirebaseMessagingException Firebase 메시징 예외
   */
  @PostMapping("/send/noti")
  public String sendTestNotification(@AuthenticationPrincipal AccountAuthDTO account)
      throws FirebaseMessagingException {

    return firebaseService.sendNotification(
        account.getPushToken(),
        "제목 테스트",
        "내용 테스트"
    );
  }

  // ========== Private Helper Methods ==========

  /**
   * 관리자 권한 여부를 확인합니다.
   */
  private boolean isAdminUser(AccountAuthDTO account) {
    return RoleType.ADMIN.name().equals(account.getRole().name());
  }

  /**
   * 관리자의 계정 조회 요청을 처리합니다.
   */
  private ResponseEntity<Object> handleAdminAccountRequest() {
    List<Account> allAccounts = accountService.getAccountAll();
    return ResponseEntity.ok(allAccounts);
  }

  /**
   * 일반 사용자의 계정 조회 요청을 처리합니다.
   */
  private ResponseEntity<Object> handleUserAccountRequest(AccountAuthDTO account) {
    Optional<List<Account>> accountList = accountService.getAccountByEcclesiaUid(
        account.getEcclesiaUid()
    );

    return accountList
        .map(accounts -> ResponseEntity.ok((Object) accounts))
        .orElseGet(() -> ResponseEntity.ok(List.of()));
  }

  /**
   * 계정 상태를 검증합니다.
   */
  private ErrorResponseType validateAccountStatus(AccountAuthDTO account) {
    if (ObjectUtils.isEmpty(account.getEcclesiaUid())) {
      return ErrorResponseType.ECCL_101;
    }

    if (!EcclesiaStatusType.APPROVAL.getName().equals(account.getEcclesiaStatus())) {
      return ErrorResponseType.ECCL_102;
    }

    return null;
  }

  /**
   * 이메일로 계정을 조회합니다.
   */
  private Account findAccountByEmail(String email) {
    return accountService.getAccountByEmail(email)
        .orElseThrow(() -> new NoSuchElementException(
            "계정을 찾을 수 없습니다. 이메일: " + email
        ));
  }

  /**
   * 계정에 푸시 토큰이 있는지 확인합니다.
   */
  private boolean hasPushToken(Account account) {
    return !ObjectUtils.isEmpty(account.getPushToken());
  }

  /**
   * QR 로그인 푸시 알림을 전송합니다.
   */
  private void sendQrLoginNotification(Account account, String code) {
    Map<String, String> pushData = new HashMap<>();
    pushData.put(PushNotificationDataType.ROUTE.lower(), DeepLinkRouterPath.QR_SCANNER.path());

    firebaseService.sendNotification(
        account.getPushToken(),
        "O O G",
        "앱을 사용해 로그인 해주세요.",
        pushData
    );
  }
}
