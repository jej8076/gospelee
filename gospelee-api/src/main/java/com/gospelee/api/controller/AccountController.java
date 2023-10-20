package com.gospelee.api.controller;

import com.gospelee.api.dto.AccountDto;
import com.gospelee.api.entity.Account;
import com.gospelee.api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import util.FieldUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/test")
@Validated
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/account/{id}")
    public ResponseEntity<Object> getAccountById(@PathVariable(name = "id") String id) {
        return new ResponseEntity<>(accountService.getAccount(id)
                .orElseThrow(() -> new NoSuchElementException("unknown loginid")), HttpStatus.OK);
    }

    @PostMapping("/account")
    public ResponseEntity<Object> saveAccount(@RequestBody @Valid AccountDto accountDto) throws IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Account account = (Account) FieldUtil.copyFields(accountDto);
        accountService.createAccount(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}