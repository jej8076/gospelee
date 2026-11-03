package com.gospelee.api.dto.pushhistory;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushHistoryDetailDTO {

  private final PushHistoryDTO push;
  private final List<Receiver> receivers;

  @Getter
  @Builder
  public static class Receiver {

    private final Long accountUid;
    private final String name;
    /** READY(전달됨) / RECEIVED / CONFIRMED / FAILED */
    private final String status;
  }
}
