package com.gospelee.api.dto.file;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUploadResponseDTO {

  private String savedFileName;
  private Long fileId;
  private Long fileDetailId;
  private String accessToken;

  @Builder
  public FileUploadResponseDTO(String savedFileName, Long fileId, Long fileDetailId, String accessToken) {
    this.savedFileName = savedFileName;
    this.fileId = fileId;
    this.fileDetailId = fileDetailId;
    this.accessToken = accessToken;
  }
}
