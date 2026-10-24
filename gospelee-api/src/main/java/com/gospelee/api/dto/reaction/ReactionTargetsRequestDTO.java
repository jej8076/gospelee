package com.gospelee.api.dto.reaction;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReactionTargetsRequestDTO {

  private List<Long> ids;
}
