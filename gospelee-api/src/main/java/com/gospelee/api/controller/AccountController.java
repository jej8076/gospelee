package com.gospelee.api.controller;

import com.gospelee.api.dto.Account.AccountDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.service.AccountService;
import jakarta.validation.Valid;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.FieldUtil;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/account")
public class AccountController {

  private final AccountService accountService;

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
   * @param accountDTO
   * @return
   */
  @PostMapping("/kakao/login")
  public ResponseEntity<Object> saveAccount(final @RequestBody @Valid AccountDTO accountDTO) {
    Account account = (Account) FieldUtil.toEntity(accountDTO);
    return new ResponseEntity<>(accountService.saveAccount(account)
        .orElseThrow(() -> new RuntimeException(
            "계정 조회 혹은 등록 실패 : [" + "accountVo : " + accountDTO.getPhone() + "]")), HttpStatus.OK);
  }

  @GetMapping("/get/{token}")
  public ResponseEntity<Object> kakaoAuthorize(@PathVariable("token") String token) {
    return new ResponseEntity<>(accountService.getAccountByToken(token)
        .orElseThrow(() -> new NoSuchElementException("fail" + "code : " + token + "]")),
        HttpStatus.OK);
  }

}