package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.Yn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "gallery_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class GalleryImage extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column(name = "ecclesia_uid", nullable = false)
  private Long ecclesiaUid;

  @Column(name = "folder_id")
  private Long folderId;

  @Column(name = "file_id", nullable = false)
  private Long fileId;

  @Column(name = "original_file_detail_id", nullable = false)
  private Long originalFileDetailId;

  @Column(name = "thumbnail_file_detail_id")
  private Long thumbnailFileDetailId;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "extension", length = 20)
  private String extension;

  @Column(name = "target_roles", length = 500)
  private String targetRoles;

  @Column(name = "del_yn", length = 1)
  private String delYn;

  @Builder
  public GalleryImage(Long id, Long ecclesiaUid, Long folderId, Long fileId,
      Long originalFileDetailId, Long thumbnailFileDetailId, Long fileSize,
      String extension, String targetRoles, String delYn) {
    this.id = id;
    this.ecclesiaUid = ecclesiaUid;
    this.folderId = folderId;
    this.fileId = fileId;
    this.originalFileDetailId = originalFileDetailId;
    this.thumbnailFileDetailId = thumbnailFileDetailId;
    this.fileSize = fileSize != null ? fileSize : 0L;
    this.extension = extension;
    this.targetRoles = targetRoles;
    this.delYn = delYn != null ? delYn : Yn.N.name();
  }

  public void changeFolder(Long folderId) {
    this.folderId = folderId;
  }

  public void changeTargetRoles(String targetRoles) {
    this.targetRoles = targetRoles;
  }

  public void markAsDeleted() {
    this.delYn = Yn.Y.name();
  }
}
