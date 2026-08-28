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
@Table(name = "gallery_folder")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class GalleryFolder extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column(name = "ecclesia_uid", nullable = false)
  private Long ecclesiaUid;

  @Column(name = "parent_id")
  private Long parentId;

  @Column(name = "depth", nullable = false)
  private Integer depth;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "target_roles", length = 500)
  private String targetRoles;

  @Column(name = "sort_order")
  private Integer sortOrder;

  @Column(name = "del_yn", length = 1)
  private String delYn;

  @Builder
  public GalleryFolder(Long id, Long ecclesiaUid, Long parentId, Integer depth, String name,
      String targetRoles, Integer sortOrder, String delYn) {
    this.id = id;
    this.ecclesiaUid = ecclesiaUid;
    this.parentId = parentId;
    this.depth = depth != null ? depth : 1;
    this.name = name;
    this.targetRoles = targetRoles;
    this.sortOrder = sortOrder != null ? sortOrder : 0;
    this.delYn = delYn != null ? delYn : Yn.N.name();
  }

  public void changeName(String name) {
    this.name = name;
  }

  public void changeTargetRoles(String targetRoles) {
    this.targetRoles = targetRoles;
  }

  public void changeParent(Long parentId, Integer depth) {
    this.parentId = parentId;
    this.depth = depth != null ? depth : 1;
  }

  public void changeSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public void markAsDeleted() {
    this.delYn = Yn.Y.name();
  }
}
