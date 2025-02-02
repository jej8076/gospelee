package com.gospelee.api.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.account.AccountDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaRequestDTO;
import com.gospelee.api.dto.firebase.PushTokenRequest;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.QrLogin;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.FirebaseService;
import com.gospelee.api.service.QrloginService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/account")
public class AccountController {

  private final AccountService accountService;

  private final QrloginService qrloginService;

  private final FirebaseService firebaseService;

  @PostMapping("/all")
  public ResponseEntity<Object> getAccountAll() {
    List<Account> getAccountAll = accountService.getAccountAll();
    return new ResponseEntity<>(getAccountAll, HttpStatus.OK);
  }

  @PostMapping("/getAccount/list")
  public ResponseEntity<Object> getAccountByEcclesiaUid(
      @AuthenticationPrincipal AccountAuthDTO account, EcclesiaRequestDTO ecclesiaRequestDTO) {
    if (RoleType.ADMIN.getName().equals(account.getRole().getName())) {
      if (ObjectUtils.isEmpty(ecclesiaRequestDTO.getEcclesiaUid())) {
        List<Account> getAccountAll = accountService.getAccountAll();
        return new ResponseEntity<>(getAccountAll, HttpStatus.OK);
      }
      return new ResponseEntity<>(
          accountService.getAccountByEcclesiaUid(ecclesiaRequestDTO.getEcclesiaUid()),
          HttpStatus.OK);
    }
    return new ResponseEntity<>(accountService.getAccountByEcclesiaUid(account.getEcclesiaUid()),
        HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> getAccountById(@PathVariable(name = "id") String id) {
    return new ResponseEntity<>(accountService.getAccountByPhone(id)
        .orElseThrow(
            () -> new NoSuchElementException("존재하는 핸드폰 번호가 없습니다 : [" + "phone : " + id + "]")),
        HttpStatus.OK);
  }

  /**
   * @param account(security에서 id_token에 대해 인증 완료한 후 데이터 조회한 결과)
   * @return
   */
  @PostMapping("/auth")
  public ResponseEntity<Object> getAccount(@AuthenticationPrincipal AccountAuthDTO account) {
    return new ResponseEntity<>(account, HttpStatus.OK);
  }

  /**
   * 카카오 API를 사용해 클라이언트 자체 로그인 성공 후 return된 결과로 이 API를 호출하여 계정정보와 토큰을 저장하며 최종 로그인 성공시키는 API
   *
   * @param account(security에서 id_token에 대해 인증 완료한 후 데이터 조회한 결과)
   * @return
   */
  @PostMapping("/kakao/getAccount")
  public ResponseEntity<Object> saveAccount(@AuthenticationPrincipal AccountAuthDTO account,
      @RequestBody PushTokenRequest pushTokenRequest) {
    accountService.savePushToken(account.getUid(), pushTokenRequest.getPushToken());
    // save 결과와 상관없이 principal 정보를 return 한다
    return new ResponseEntity<>(account, HttpStatus.OK);
  }

  @PostMapping("/qr/enter")
  public ResponseEntity<Object> qrEnter(@RequestBody AccountDTO.QrRequest qrRequest) {
    QrLogin qrLogin = qrloginService.saveQrlogin(qrRequest.getEmail());
    Map<String, String> pushSendDataMap = new HashMap<>();
    pushSendDataMap.put("code", qrLogin.getCode());

    Account account = accountService.getAccountByEmail(qrRequest.getEmail()).orElseThrow(
        () -> new NoSuchElementException(
            "Not found Account By email : " + "[" + qrRequest.getEmail() + "]"));
    String pushToken = account.getPushToken();

    if (ObjectUtils.isEmpty(pushToken)) {
      return new ResponseEntity<>(qrLogin, HttpStatus.OK);
    }

    firebaseService.sendNotification(
        account.getPushToken(),
        "O O G",
        "앱을 사용해 로그인 해주세요.",
        pushSendDataMap
    );

    return new ResponseEntity<>(qrLogin, HttpStatus.OK);
  }

  /**
   * <pre>
   * 앱에서 관리자페이지의 qr코드를 스캔할 때 호출되는 부분 code는 랜덤 수이며
   * 앱에서 qr코드를 스캔(http request) 할때 header에 token 값을 넣어주고 이쪽으로 오기 전에 인증을 거친다
   * 인증에 성공하면 이쪽에서 code에 해당하는 데이터에 token을 넣어준다
   * </pre>
   *
   * @param account
   * @param code
   * @return
   */
  @PostMapping("/qr/req/{code}")
  public ResponseEntity<Object> qrAuth(
      @AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable(value = "code") String code
  ) {
    HttpStatus status = HttpStatus.OK;
    if (!qrloginService.updateQrlogin(account, code)) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    // qrLogin 데이터가 존재하면 로그인 성공한 것으로 생각하면 되며 websocket을 사용하던지 해서 로그인된 페이지로 이동시키면 된다
    return new ResponseEntity<>("", status);
  }

  /**
   * <pre>
   * 프론트에서 주기적으로 이 api를 호출하여 QrLogin 테이블에 code를 확인하여 token이 있는 지 확인하는 것이 목적이다
   * 나중에 이 부분을 웹소켓으로 대체하여야 한다
   * </pre>
   *
   * @param qrCheckRequest
   * @return
   */
  @PostMapping("/qr/check")
  public ResponseEntity<Object> qrAuthCheck(
      HttpServletResponse response,
      @RequestBody AccountDTO.QrCheckRequest qrCheckRequest
  ) {
    QrLogin qrLogin = qrloginService.getQrLogin(qrCheckRequest.getEmail(),
        qrCheckRequest.getCode());

    return new ResponseEntity<>(qrLogin, HttpStatus.OK);
  }

  @PostMapping("/send/noti")
  public String sendNotification(
      @AuthenticationPrincipal AccountAuthDTO account
  ) throws FirebaseMessagingException {
    return firebaseService.sendNotification(
        account.getPushToken(),
        "제목테스트!!!!!!!!!!!!!!!!!!",
        "내용테스트!!!!!!!!!!!!!!"
    );
  }

}
