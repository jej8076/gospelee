package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "File")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class FileEntity extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column
  private String parentId;

  @Column
  private Long accountUid;

  @Column
  private String category;

  @Column
  private Integer totalCount;

  @Column
  private String delYn;

  @Column
  private String accessToken;

  @Builder
  public FileEntity(Long id, String parentId, Long accountUid, String category, Integer totalCount,
      String delYn, String accessToken) {
    this.id = id;
    this.parentId = parentId;
    this.accountUid = accountUid;
    this.category = category;
    this.totalCount = totalCount;
    this.delYn = delYn;
    this.accessToken = accessToken;
  }
}
