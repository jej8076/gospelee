package com.gospelee.api.controller;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.common.DataResponseDTO;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.service.ministry.AdminMinistryService;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 모임 목록·정지 API (ADMIN 전용) */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/ministries")
public class AdminMinistryController {

  private final AdminMinistryService adminMinistryService;

  public record ListRequest(String keyword) {

  }

  public record StatusRequest(boolean suspend) {

  }

  @PostMapping("/list")
  public ResponseEntity<Object> list(@AuthenticationPrincipal AccountAuthDTO account,
      @RequestBody(required = false) ListRequest request) {
    return run(account,
        () -> adminMinistryService.list(request == null ? null : request.keyword()));
  }

  @PostMapping("/{id}/status")
  public ResponseEntity<Object> changeStatus(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable Long id, @RequestBody StatusRequest request) {
    return run(account,
        () -> adminMinistryService.changeStatus(id, request.suspend(), account.getUid()));
  }

  private ResponseEntity<Object> run(AccountAuthDTO account, Supplier<Object> action) {
    if (account == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (account.getRole() != RoleType.ADMIN) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(DataResponseDTO.of("100", "성공", action.get()));
  }
}
