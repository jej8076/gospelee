package com.gospelee.api.controller;

import com.gospelee.api.dto.pushhistory.PushReceiptRequestDTO;
import com.gospelee.api.service.push.PushReceiptService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 앱이 푸시 수신·확인을 알리는 API. 로그인한 사용자 본인의 기록만 바뀐다 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/push-receipt")
public class PushReceiptController {

  private final PushReceiptService pushReceiptService;

  @PostMapping
  public ResponseEntity<Object> report(@RequestBody @Valid PushReceiptRequestDTO body) {
    return new ResponseEntity<>(Map.of("updated", pushReceiptService.report(body)),
        HttpStatus.OK);
  }
}
