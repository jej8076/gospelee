package com.gospelee.api.controller;

import com.gospelee.api.service.push.PushHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 푸시 발송 현황 API. 교회 관리자 전용 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/push-history")
public class PushHistoryController {

  private final PushHistoryService pushHistoryService;

  @GetMapping
  public ResponseEntity<Object> getList() {
    return new ResponseEntity<>(pushHistoryService.getList(), HttpStatus.OK);
  }

  @GetMapping("/{pushId}")
  public ResponseEntity<Object> getDetail(@PathVariable("pushId") Long pushId) {
    return new ResponseEntity<>(pushHistoryService.getDetail(pushId), HttpStatus.OK);
  }
}
