package com.gospelee.api.dto.file;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUploadDetailResponseDTO {

  private Long fileDetailId;
  private String fileOriginalName;

  @Builder
  public FileUploadDetailResponseDTO(Long fileDetailId, String fileOriginalName) {
    this.fileDetailId = fileDetailId;
    this.fileOriginalName = fileOriginalName;
  }
}
