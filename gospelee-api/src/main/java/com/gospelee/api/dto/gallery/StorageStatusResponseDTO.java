package com.gospelee.api.dto.gallery;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class StorageStatusResponseDTO {

  private Long ecclesiaUid;
  private String ecclesiaName;
  private Long storageLimitBytes;
  private Long storageUsedBytes;
  private Double usedPercentage;
  private String formattedLimit;
  private String formattedUsed;

  @Builder
  public StorageStatusResponseDTO(Long ecclesiaUid, String ecclesiaName, Long storageLimitBytes,
      Long storageUsedBytes, Double usedPercentage, String formattedLimit, String formattedUsed) {
    this.ecclesiaUid = ecclesiaUid;
    this.ecclesiaName = ecclesiaName;
    this.storageLimitBytes = storageLimitBytes;
    this.storageUsedBytes = storageUsedBytes;
    this.usedPercentage = usedPercentage;
    this.formattedLimit = formattedLimit;
    this.formattedUsed = formattedUsed;
  }
}
