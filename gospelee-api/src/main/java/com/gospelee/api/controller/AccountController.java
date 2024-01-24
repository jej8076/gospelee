package com.gospelee.api.controller;

import com.gospelee.api.dto.AccountVo;
import com.gospelee.api.entity.Account;
import com.gospelee.api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import util.FieldUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAccountById(@PathVariable(name = "id") String id) {
        return new ResponseEntity<>(accountService.getAccountByPhone(id)
                .orElseThrow(() -> new NoSuchElementException("존재하는 핸드폰 번호가 없습니다 : [" + "phone : " + id + "]")), HttpStatus.OK);
    }

    /**
     * 카카오 API를 사용해 클라이언트 자체 로그인 성공 후 return된 결과로 이 API를 호출하여
     * 계정정보와 토큰을 저장하며 최종 로그인 성공시키는 API
     * @param accountVo
     * @return
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @PostMapping("/kakao/login")
    public ResponseEntity<Object> saveAccount(final @RequestBody @Valid AccountVo accountVo) throws IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Account account = (Account) FieldUtil.toEntity(accountVo);
        return new ResponseEntity<>(accountService.createAccount(account)
                .orElseThrow(() -> new RuntimeException("계정 조회 혹은 등록 실패 : [" + "accountVo : " + accountVo.getPhone() + "]")), HttpStatus.OK);
    }

    @PutMapping("/kakao/login")
    public ResponseEntity<Object> kakaoAuthorize(@RequestBody String code) {
        return new ResponseEntity<>(accountService.getKakaoAuthorize(code)
                .orElseThrow(() -> new NoSuchElementException("fail" + "code : " + code + "]")), HttpStatus.OK);
    }

}