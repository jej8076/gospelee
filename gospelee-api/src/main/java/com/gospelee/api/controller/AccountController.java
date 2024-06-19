package com.gospelee.api.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.dto.account.AccountDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaDTO;
import com.gospelee.api.dto.firebase.PushTokenRequest;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.QrLogin;
import com.gospelee.api.entity.RoleType;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.FirebaseService;
import com.gospelee.api.service.QrloginService;
import com.gospelee.api.service.RedisCacheService;
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

  private final RedisCacheService redisCacheService;

  @PostMapping("/all")
  public ResponseEntity<Object> getAccountAll() {
    List<Account> getAccountAll = accountService.getAccountAll();
    return new ResponseEntity<>(getAccountAll, HttpStatus.OK);
  }

  @PostMapping("/getAccount")
  public ResponseEntity<Object> getAccountByEcclesiaUid(@AuthenticationPrincipal Account account,
      EcclesiaDTO ecclesiaDTO) {
    if (RoleType.ADMIN.getName().equals(account.getRole().getName())) {
      if (ObjectUtils.isEmpty(ecclesiaDTO.getEcclesiaUid())) {
        List<Account> getAccountAll = accountService.getAccountAll();
        return new ResponseEntity<>(getAccountAll, HttpStatus.OK);
      }
      return new ResponseEntity<>(
          accountService.getAccountByEcclesiaUid(ecclesiaDTO.getEcclesiaUid()), HttpStatus.OK);
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
   * 카카오 API를 사용해 클라이언트 자체 로그인 성공 후 return된 결과로 이 API를 호출하여 계정정보와 토큰을 저장하며 최종 로그인 성공시키는 API
   *
   * @param account(security에서 id_token에 대해 인증 완료한 후 데이터 조회한 결과)
   * @return
   */
  @PostMapping("/kakao/getAccount")
  public ResponseEntity<Object> saveAccount(@AuthenticationPrincipal Account account,
      @RequestBody PushTokenRequest pushTokenRequest) {
    accountService.savePushToken(account.getUid(), pushTokenRequest.getPushToken());
    // save 결과와 상관없이 principal 정보를 return 한다
    return new ResponseEntity<>(account, HttpStatus.OK);
  }

  @PostMapping("/qr/enter")
  public ResponseEntity<Object> qrEnter(@RequestBody AccountDTO.QrRequest qrRequest)
      throws FirebaseMessagingException {
    QrLogin qrLogin = qrloginService.saveQrlogin(qrRequest.getEmail());
    Map<String, String> pushSendDataMap = new HashMap<>();
    pushSendDataMap.put("code", qrLogin.getCode());
    Account account = accountService.getAccountByEmail(qrRequest.getEmail()).orElseThrow(
        () -> new NoSuchElementException(
            "Not found Account By email : " + "[" + qrRequest.getEmail() + "]"));
    System.out.println("account.getPushToken() = " + account.getPushToken());
    firebaseService.sendNotification(
        account.getPushToken(),
        "O O G",
        "앱을 사용해 로그인 해주세요.",
        pushSendDataMap
    );
    return new ResponseEntity<>(qrLogin, HttpStatus.OK);
  }

  // TODO 이쪽으로 호출이 들어오기 전에 토큰 검증을 거치고, 검증이 되면 이쪽에서 검증 완료를 업데이트 한다
  // 검증 완료 됐다는 것은 admin front에 로그인 처리된 후의 화면을 보여주어도 된다는 뜻이다
  // 추후 이 API를 사용하지 않고 앱에서 qr스캔하면 websocket을 호출하여 인증 후 바로 로그인 성공 페이지로 이동되도록 해야한다
  @PostMapping("/qr/{code}")
  public ResponseEntity<Object> qrAuth(
      @AuthenticationPrincipal Account account,
      @PathVariable(value = "code") String code
  ) {
    QrLogin qrLogin = qrloginService.getQrLogin(account.getEmail(), code);

    // qrLogin 데이터가 존재하면 로그인 성공한 것으로 생각하면 되며 websocket을 사용하던지 해서 로그인된 페이지로 이동시키면 된다
    return new ResponseEntity<>(qrLogin, HttpStatus.OK);
  }

  @GetMapping("/send/noti")
  public String sendNotification() throws FirebaseMessagingException {
    return firebaseService.sendNotification(
        "et-zrxEMRzKaB9rtFaz2C9:APA91bEUurIAT2Mfm3YrsDtGUKztLiFeWKmipKetErvTfMpeqcXa3j3rku5tSxh1f11jYfO7ES_HPkulAIMvw4-1H0h1AlpxF3w6q5mmfBgsduFvk_t79tofG6g_v7FHvjQ1eUteSQCa",
        "제목~~!",
        "내용~!!!"
    );
  }

}
