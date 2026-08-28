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
public class GalleryImageResponseDTO {

  private Long id;
  private Long ecclesiaUid;
  private Long folderId;
  private String folderName;
  private Long fileId;
  private Long originalFileDetailId;
  private Long thumbnailFileDetailId;
  private String originalUrl;
  private String thumbnailUrl;
  private String originalFileName;
  private Long fileSize;
  private String extension;
  private List<String> targetRoles;
  private LocalDateTime insertTime;

  @Builder
  public GalleryImageResponseDTO(Long id, Long ecclesiaUid, Long folderId, String folderName,
      Long fileId, Long originalFileDetailId, Long thumbnailFileDetailId, String originalUrl,
      String thumbnailUrl, String originalFileName, Long fileSize, String extension,
      List<String> targetRoles, LocalDateTime insertTime) {
    this.id = id;
    this.ecclesiaUid = ecclesiaUid;
    this.folderId = folderId;
    this.folderName = folderName;
    this.fileId = fileId;
    this.originalFileDetailId = originalFileDetailId;
    this.thumbnailFileDetailId = thumbnailFileDetailId;
    this.originalUrl = originalUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.originalFileName = originalFileName;
    this.fileSize = fileSize;
    this.extension = extension;
    this.targetRoles = targetRoles;
    this.insertTime = insertTime;
  }
}
