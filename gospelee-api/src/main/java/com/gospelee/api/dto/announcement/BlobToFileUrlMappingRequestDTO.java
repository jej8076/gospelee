package com.gospelee.api.dto.announcement;

import com.gospelee.api.dto.file.FileUploadDetailResponseDTO;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlobToFileUrlMappingRequestDTO {

  private List<FileUploadDetailResponseDTO> fileDetailList;
  private Map<String, String> blobFileMapping;
  private String serverDomain;
  private String accessToken;

  @Builder
  public BlobToFileUrlMappingRequestDTO(List<FileUploadDetailResponseDTO> fileDetailList,
      Map<String, String> blobFileMapping, String serverDomain, String accessToken) {
    this.fileDetailList = fileDetailList;
    this.blobFileMapping = blobFileMapping;
    this.serverDomain = serverDomain;
    this.accessToken = accessToken;
  }
}
