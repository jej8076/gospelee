package com.gospelee.api.dto.file;

import com.gospelee.api.entity.FileDetails;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FileDetailsDTO {

  private final Long id;
  private final Long fileId;
  private final String filePath;
  private final String fileOriginalName;
  private final Long fileSize;
  private final String fileType;
  private final String extension;
  private final String delYn;

  @Builder
  public FileDetailsDTO(Long id, Long fileId, String filePath, String fileOriginalName,
      Long fileSize,
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


  /**
   * Entity → DTO 변환
   */
  public static FileDetailsDTO fromEntity(FileDetails entity) {
    return new FileDetailsDTO(
        entity.getId(),
        entity.getFileId(),
        entity.getFilePath(),
        entity.getFileOriginalName(),
        entity.getFileSize(),
        entity.getFileType(),
        entity.getExtension(),
        entity.getDelYn()
    );
  }

  /**
   * DTO → Entity 변환
   */
  public FileDetails toEntity() {
    return FileDetails.builder()
        .id(this.id)
        .fileId(this.fileId)
        .filePath(this.filePath)
        .fileOriginalName(this.fileOriginalName)
        .fileSize(this.fileSize)
        .fileType(this.fileType)
        .extension(this.extension)
        .delYn(this.delYn)
        .build();
  }
}