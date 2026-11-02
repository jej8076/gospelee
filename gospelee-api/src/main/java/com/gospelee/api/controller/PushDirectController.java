package com.gospelee.api.controller;

import com.gospelee.api.dto.pushdirect.PushDirectRequestDTO;
import com.gospelee.api.service.push.PushDirectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 공지·주보 등록 없이 푸시를 직접 보내는 API. 교회 관리자 전용 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/push-direct")
public class PushDirectController {

  private final PushDirectService pushDirectService;

  @PostMapping
  public ResponseEntity<Object> send(@RequestBody @Valid PushDirectRequestDTO body) {
    return new ResponseEntity<>(pushDirectService.send(body), HttpStatus.OK);
  }
}
