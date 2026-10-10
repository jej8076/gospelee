package com.gospelee.api.controller;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.common.DataResponseDTO;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.BlockRequest;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.CommentRequest;
import com.gospelee.api.dto.journalshare.JournalShareDTOs.ReportRequest;
import com.gospelee.api.service.JournalShareException;
import com.gospelee.api.service.JournalShareService;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 묵상 공유 / 댓글 / 신고 / 차단 API (로그인 필요). 비회원용 웹 페이지는 {@link JournalSharePageController}
 */
@RestController
@RequiredArgsConstructor
@RequestMapping
public class JournalShareController {

  private final JournalShareService shareService;

  @PostMapping("/journal/{journalUid}/share")
  public ResponseEntity<Object> share(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable long journalUid) {
    return run(account, () -> shareService.share(account.getUid(), journalUid));
  }

  @DeleteMapping("/journal/{journalUid}/share")
  public ResponseEntity<Object> revoke(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable long journalUid) {
    return run(account, () -> {
      shareService.revoke(account.getUid(), journalUid);
      return null;
    });
  }

  @GetMapping("/journal/share/{token}")
  public ResponseEntity<Object> view(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable String token) {
    return run(account, () -> shareService.view(account.getUid(), token));
  }

  @PostMapping("/journal/share/{token}/comments")
  public ResponseEntity<Object> addComment(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable String token, @RequestBody CommentRequest request) {
    return run(account, () -> shareService.addComment(account.getUid(), token, request.content()));
  }

  @DeleteMapping("/journal/share/comments/{commentUid}")
  public ResponseEntity<Object> deleteComment(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable long commentUid) {
    return run(account, () -> {
      shareService.deleteComment(account.getUid(), commentUid);
      return null;
    });
  }

  @PostMapping("/journal/share/report")
  public ResponseEntity<Object> report(@AuthenticationPrincipal AccountAuthDTO account,
      @RequestBody ReportRequest request) {
    return run(account, () -> {
      shareService.report(account.getUid(), request.targetType(), request.targetUid(),
          request.reason(), request.detail());
      return null;
    });
  }

  @PostMapping("/account/block")
  public ResponseEntity<Object> block(@AuthenticationPrincipal AccountAuthDTO account,
      @RequestBody BlockRequest request) {
    return run(account, () -> {
      shareService.block(account.getUid(), request.accountUid());
      return null;
    });
  }

  @DeleteMapping("/account/block/{blockedUid}")
  public ResponseEntity<Object> unblock(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable long blockedUid) {
    return run(account, () -> {
      shareService.unblock(account.getUid(), blockedUid);
      return null;
    });
  }

  @GetMapping("/account/block")
  public ResponseEntity<Object> blockedUsers(@AuthenticationPrincipal AccountAuthDTO account) {
    return run(account, () -> shareService.blockedUsers(account.getUid()));
  }

  /** 성공은 code 100, 처리된 실패는 code 에 사유를 담아 200 으로 내려준다 (기존 API 응답 규칙) */
  private ResponseEntity<Object> run(AccountAuthDTO account, Supplier<Object> action) {
    if (account == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    try {
      return ResponseEntity.ok(DataResponseDTO.of("100", "성공", action.get()));
    } catch (JournalShareException e) {
      return ResponseEntity.ok(DataResponseDTO.of(e.getCode(), e.getMessage(), null));
    }
  }
}
