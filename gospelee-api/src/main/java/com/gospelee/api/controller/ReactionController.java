package com.gospelee.api.controller;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.reaction.ReactionMinePageDTO;
import com.gospelee.api.dto.reaction.ReactionSummaryDTO;
import com.gospelee.api.dto.reaction.ReactionTargetsRequestDTO;
import com.gospelee.api.enums.ReactionTargetType;
import com.gospelee.api.enums.ReactionType;
import com.gospelee.api.service.reaction.ReactionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 반응(기도할게요 등) API. 반응 종류와 대상 종류를 경로로 받아 어떤 화면에서든 같은 방식으로 쓴다.
 *
 * <pre>
 *   PUT    /reaction/{type}/{targetType}/{targetId}   반응 켜기 (멱등)
 *   DELETE /reaction/{type}/{targetType}/{targetId}   반응 취소 (멱등)
 *   POST   /reaction/{type}/{targetType}/summary      여러 대상의 수 + 내 반응 여부
 *   GET    /reaction/{type}/mine                      내가 반응한 목록 (최신순, 커서)
 * </pre>
 */
@RestController
@RequestMapping("/reaction")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;

  @PutMapping("/{type}/{targetType}/{targetId}")
  public ResponseEntity<ReactionSummaryDTO> on(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable ReactionType type, @PathVariable ReactionTargetType targetType,
      @PathVariable Long targetId) {
    return ResponseEntity.ok(reactionService.set(account, type, targetType, targetId, true));
  }

  @DeleteMapping("/{type}/{targetType}/{targetId}")
  public ResponseEntity<ReactionSummaryDTO> off(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable ReactionType type, @PathVariable ReactionTargetType targetType,
      @PathVariable Long targetId) {
    return ResponseEntity.ok(reactionService.set(account, type, targetType, targetId, false));
  }

  @PostMapping("/{type}/{targetType}/summary")
  public ResponseEntity<List<ReactionSummaryDTO>> summary(
      @AuthenticationPrincipal AccountAuthDTO account, @PathVariable ReactionType type,
      @PathVariable ReactionTargetType targetType, @RequestBody ReactionTargetsRequestDTO body) {
    return ResponseEntity.ok(
        reactionService.summarize(account.getUid(), type, targetType, body.getIds()));
  }

  @GetMapping("/{type}/mine")
  public ResponseEntity<ReactionMinePageDTO> mine(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable ReactionType type,
      @RequestParam(required = false) ReactionTargetType targetType,
      @RequestParam(required = false) Long cursor,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        reactionService.listMine(account.getUid(), type, targetType, cursor, size));
  }
}
