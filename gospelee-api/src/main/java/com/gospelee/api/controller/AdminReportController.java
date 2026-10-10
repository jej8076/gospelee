package com.gospelee.api.controller;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.common.DataResponseDTO;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.service.AdminReportService;
import com.gospelee.api.service.JournalShareException;
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

/** 신고된 묵상 공유/댓글 검토 API (ADMIN 전용) */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/content-reports")
public class AdminReportController {

  private final AdminReportService adminReportService;

  public record ListRequest(String status) {

  }

  public record ResolveRequest(String action) {

  }

  @PostMapping("/list")
  public ResponseEntity<Object> list(@AuthenticationPrincipal AccountAuthDTO account,
      @RequestBody(required = false) ListRequest request) {
    return run(account, () -> adminReportService.list(request == null ? null : request.status()));
  }

  @PostMapping("/{reportUid}/resolve")
  public ResponseEntity<Object> resolve(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable long reportUid, @RequestBody ResolveRequest request) {
    return run(account, () -> {
      adminReportService.resolve(reportUid, request.action(), account.getUid());
      return null;
    });
  }

  private ResponseEntity<Object> run(AccountAuthDTO account, Supplier<Object> action) {
    if (account == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (account.getRole() != RoleType.ADMIN) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
      return ResponseEntity.ok(DataResponseDTO.of("100", "성공", action.get()));
    } catch (JournalShareException e) {
      return ResponseEntity.ok(DataResponseDTO.of(e.getCode(), e.getMessage(), null));
    }
  }
}
