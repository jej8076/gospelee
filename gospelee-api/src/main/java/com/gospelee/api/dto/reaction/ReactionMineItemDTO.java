package com.gospelee.api.dto.reaction;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** "내가 표시한 것 모아보기" 한 줄 */
@Getter
@AllArgsConstructor
public class ReactionMineItemDTO {

  /** 다음 페이지 커서로 쓰는 반응 uid */
  private final Long uid;
  private final String targetType;
  private final Long targetId;
  private final LocalDateTime reactedAt;
}
