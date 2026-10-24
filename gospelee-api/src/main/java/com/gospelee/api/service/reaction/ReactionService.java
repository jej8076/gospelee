package com.gospelee.api.service.reaction;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.reaction.ReactionMinePageDTO;
import com.gospelee.api.dto.reaction.ReactionSummaryDTO;
import com.gospelee.api.enums.ReactionTargetType;
import com.gospelee.api.enums.ReactionType;
import java.util.List;

/**
 * 반응(기도할게요 등) 공통 서비스. 대상 종류·반응 종류에 상관없이 같은 방식으로 동작한다.
 */
public interface ReactionService {

  /**
   * 반응을 켜거나 끈다. 이미 그 상태면 아무것도 바꾸지 않는다 (멱등).
   *
   * @return 처리 후의 반응 현황
   */
  ReactionSummaryDTO set(AccountAuthDTO account, ReactionType type, ReactionTargetType targetType,
      Long targetId, boolean active);

  /**
   * 여러 대상의 반응 수와 내 반응 여부. {@code accountUid} 가 null 이면 mine 은 모두 false.
   * 결과는 요청한 id 순서와 같다.
   */
  List<ReactionSummaryDTO> summarize(Long accountUid, ReactionType type,
      ReactionTargetType targetType, List<Long> targetIds);

  /** 내가 반응한 목록 (최신순). {@code targetType} 이 null 이면 대상 종류와 무관 */
  ReactionMinePageDTO listMine(Long accountUid, ReactionType type, ReactionTargetType targetType,
      Long cursor, int size);

  /** 대상이 삭제될 때 그 대상의 반응을 정리한다 */
  void removeAllByTarget(ReactionTargetType targetType, Long targetId);

  /** 회원 탈퇴 시 그 사용자의 반응을 정리한다 */
  void removeAllByAccount(Long accountUid);
}
