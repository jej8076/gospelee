package com.gospelee.api.dto.reaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 대상 하나의 반응 현황: 전체 수와 내가 반응했는지 */
@Getter
@AllArgsConstructor
public class ReactionSummaryDTO {

  private final Long targetId;
  private final long count;
  private final boolean mine;
}
