package com.gospelee.api.dto.youtube;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeVideoRequestDTO {

  private Long id;
  private String videoId;
  private String title;
  private String description;
  private String thumbnailUrl;
  private String channelTitle;
  private String publishedAt;
  private Boolean isActive;
  private Integer sortOrder;
}
