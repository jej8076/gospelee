package com.gospelee.api.dto.ecclesia;

import com.gospelee.api.entity.Ecclesia;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EcclesiaResponseDTO {

  private Long ecclesiaUid;
  private String churchIdentificationNumber;
  private String status;
  private String ecclesiaName;
  private String masterAccountName;
  private LocalDateTime insertTime;

  @Builder
  public EcclesiaResponseDTO(Long ecclesiaUid, String churchIdentificationNumber, String status,
      String ecclesiaName, String masterAccountName, LocalDateTime insertTime) {
    this.ecclesiaUid = ecclesiaUid;
    this.churchIdentificationNumber = churchIdentificationNumber;
    this.status = status;
    this.ecclesiaName = ecclesiaName;
    this.masterAccountName = masterAccountName;
    this.insertTime = insertTime;
  }

  public static EcclesiaResponseDTO fromEntity(Ecclesia ecclesia) {
    return EcclesiaResponseDTO.builder()
        .ecclesiaUid(ecclesia.getUid())
        .ecclesiaName(ecclesia.getName())
        .status(ecclesia.getStatus())
        .insertTime(ecclesia.getInsertTime())
        .build();
  }

}


