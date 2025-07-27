package com.gospelee.api.dto.file;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUploadResponseDTO {

  private Long fileId;
  private List<FileUploadDetailResponseDTO> fileDetailList;
  private String accessToken;

  @Builder
  public FileUploadResponseDTO(Long fileId, List<FileUploadDetailResponseDTO> fileDetailList,
      String accessToken) {
    this.fileId = fileId;
    this.fileDetailList = fileDetailList;
    this.accessToken = accessToken;
  }
}
