package com.gospelee.api.dto.pushgroup;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PushGroupResponseDTO {

  private final Long id;
  private final String name;
  private final String description;
  private final int memberCount;
  private final List<Long> memberAccountUids;
  private final LocalDateTime insertTime;

  @Builder
  public PushGroupResponseDTO(Long id, String name, String description,
      List<Long> memberAccountUids, LocalDateTime insertTime) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.memberAccountUids = memberAccountUids;
    this.memberCount = memberAccountUids.size();
    this.insertTime = insertTime;
  }
}
