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

  @Builder
  public FileUploadResponseDTO(String savedFileName) {
    this.savedFileName = savedFileName;
  }
}
