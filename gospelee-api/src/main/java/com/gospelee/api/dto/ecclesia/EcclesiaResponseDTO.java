package com.gospelee.api.dto.ecclesia;

import com.gospelee.api.dto.ecclesia.projection.EcclesiaResponseProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class EcclesiaResponseDTO {

  private Long ecclesiaUid;
  private String churchIdentificationNumber;
  private String status;
  private String ecclesiaName;
  private String masterAccountName;
  private LocalDateTime insertTime;

  public EcclesiaResponseDTO(Long ecclesiaUid, String churchIdentificationNumber, String status,
      String ecclesiaName, String masterAccountName, LocalDateTime insertTime) {
    this.ecclesiaUid = ecclesiaUid;
    this.churchIdentificationNumber = churchIdentificationNumber;
    this.status = status;
    this.ecclesiaName = ecclesiaName;
    this.masterAccountName = masterAccountName;
    this.insertTime = insertTime;
  }

  public static EcclesiaResponseDTO from(EcclesiaResponseProjection projection) {
    return new EcclesiaResponseDTO(
        projection.getEcclesiaUid(),
        projection.getChurchIdentificationNumber(),
        projection.getStatus(),
        projection.getEcclesiaName(),
        projection.getMasterAccountName(),
        projection.getInsertTime()
    );
  }

  public static List<EcclesiaResponseDTO> fromList(List<EcclesiaResponseProjection> projections) {
    return projections.stream()
        .map(EcclesiaResponseDTO::from)
        .collect(Collectors.toList());
  }
}


