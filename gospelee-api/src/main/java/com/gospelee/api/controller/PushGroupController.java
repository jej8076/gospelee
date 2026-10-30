package com.gospelee.api.controller;

import com.gospelee.api.dto.pushgroup.PushGroupRequestDTO;
import com.gospelee.api.service.push.PushGroupService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 푸시 알림 그룹 API. 교회 관리자 전용 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/push-group")
public class PushGroupController {

  private final PushGroupService pushGroupService;

  @GetMapping
  public ResponseEntity<Object> getList() {
    return new ResponseEntity<>(pushGroupService.getList(), HttpStatus.OK);
  }

  /** 그룹 멤버로 고를 수 있는 성도 목록 */
  @GetMapping("/candidates")
  public ResponseEntity<Object> getCandidates() {
    return new ResponseEntity<>(pushGroupService.getCandidates(), HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestBody @Valid PushGroupRequestDTO body) {
    return new ResponseEntity<>(pushGroupService.create(body), HttpStatus.OK);
  }

  @PutMapping
  public ResponseEntity<Object> update(@RequestBody @Valid PushGroupRequestDTO body) {
    return new ResponseEntity<>(pushGroupService.update(body), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
    pushGroupService.delete(id);
    // 앱 ApiClient 는 빈 본문을 실패로 처리하므로 삭제한 id 를 본문으로 반환
    return new ResponseEntity<>(Map.of("id", id), HttpStatus.OK);
  }
}
