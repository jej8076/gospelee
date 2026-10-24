package com.gospelee.api.dto.reaction;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReactionMinePageDTO {

  private final List<ReactionMineItemDTO> items;
  /** 더 가져올 게 없으면 null */
  private final Long nextCursor;
}
