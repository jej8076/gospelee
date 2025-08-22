package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import com.gospelee.api.enums.Yn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class FileDetails extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column
  private Long fileId;

  @Column
  private String filePath;

  @Column
  private String fileOriginalName;

  @Column
  private Long fileSize;

  @Column
  private String fileType;

  @Column
  private String extension;

  @Column
  private String delYn;

  @Builder
  public FileDetails(Long id, Long fileId, String filePath, String fileOriginalName, Long fileSize,
      String fileType, String extension, String delYn) {
    this.id = id;
    this.fileId = fileId;
    this.filePath = filePath;
    this.fileOriginalName = fileOriginalName;
    this.fileSize = fileSize;
    this.fileType = fileType;
    this.extension = extension;
    this.delYn = delYn;
  }

  public void markAsDeleted() {
    this.delYn = Yn.Y.name();
  }
}
