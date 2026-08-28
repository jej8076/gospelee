package com.gospelee.api.dto.gallery;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class GalleryImageListResponseDTO {

  private List<GalleryImageResponseDTO> content;
  private long totalElements;
  private int totalPages;
  private int page;
  private int size;

  @Builder
  public GalleryImageListResponseDTO(List<GalleryImageResponseDTO> content, long totalElements,
      int totalPages, int page, int size) {
    this.content = content;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.page = page;
    this.size = size;
  }
}
