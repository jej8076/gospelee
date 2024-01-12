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

    @PostMapping("")
    public ResponseEntity<Object> saveAccount(final @RequestBody @Valid AccountVo accountVo) throws IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Account account = (Account) FieldUtil.toEntity(accountVo);
        accountService.createAccount(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/kakao/login")
    public ResponseEntity<Object> kakaoAuthorize(@RequestBody String code) {
        return new ResponseEntity<>(accountService.getKakaoAuthorize(code)
                .orElseThrow(() -> new NoSuchElementException("fail" + "code : " + code + "]")), HttpStatus.OK);
    }

}