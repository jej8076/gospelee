package com.gospelee.api.dto.gallery;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class GalleryImageListRequestDTO {

  private Long folderId;
  private Boolean includeUncategorized; // true면 folderId==null인 루트 이미지
  private String startDate; // "yyyy-MM-dd"
  private String endDate;   // "yyyy-MM-dd"
  private String targetRole;
  private Integer page = 0;
  private Integer size = 20;
}
