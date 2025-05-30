package com.gospelee.api.dto.file;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUploadRequestDTO {

  private String filePath;
  private String fileSaveName;
  private String fileOriginalName;
  private Long fileSize;
  private String fileType;
  private String extension;

  @Builder
  public FileUploadRequestDTO(String filePath, String fileSaveName, String fileOriginalName,
      Long fileSize, String fileType, String extension) {
    this.filePath = filePath;
    this.fileSaveName = fileSaveName;
    this.fileOriginalName = fileOriginalName;
    this.fileSize = fileSize;
    this.fileType = fileType;
    this.extension = extension;
  }
}
