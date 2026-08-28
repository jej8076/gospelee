package com.gospelee.api.dto.gallery;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class GalleryFolderResponseDTO {

  private Long id;
  private Long ecclesiaUid;
  private Long parentId;
  private Integer depth;
  private String name;
  private List<String> targetRoles;
  private Integer sortOrder;
  private Long imageCount;
  private Long subFolderCount;
  private LocalDateTime insertTime;
  private LocalDateTime updateTime;

  @Builder
  public GalleryFolderResponseDTO(Long id, Long ecclesiaUid, Long parentId, Integer depth,
      String name, List<String> targetRoles, Integer sortOrder, Long imageCount,
      Long subFolderCount, LocalDateTime insertTime, LocalDateTime updateTime) {
    this.id = id;
    this.ecclesiaUid = ecclesiaUid;
    this.parentId = parentId;
    this.depth = depth;
    this.name = name;
    this.targetRoles = targetRoles;
    this.sortOrder = sortOrder;
    this.imageCount = imageCount != null ? imageCount : 0L;
    this.subFolderCount = subFolderCount != null ? subFolderCount : 0L;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
  }
}
