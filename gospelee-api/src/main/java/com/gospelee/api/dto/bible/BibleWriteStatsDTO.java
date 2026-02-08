package com.gospelee.api.dto.bible;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BibleWriteStatsDTO {
  private int totalChapters;        // 1189
  private int completedChapters;    // 완료한 장 수
  private int oldTestamentCount;    // 구약 완료 수
  private int newTestamentCount;    // 신약 완료 수
  private List<BookStatDTO> bookStats;  // 책별 통계
}
