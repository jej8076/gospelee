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

  private Long uid;
  private String churchIdentificationNumber;
  private String status;
  private String name;
  private String masterAccountName;
  private LocalDateTime insertTime;

  @Builder
  public EcclesiaResponseDTO(Long uid, String churchIdentificationNumber, String status,
      String name,
      String masterAccountName, LocalDateTime insertTime) {
    this.uid = uid;
    this.churchIdentificationNumber = churchIdentificationNumber;
    this.status = status;
    this.name = name;
    this.masterAccountName = masterAccountName;
    this.insertTime = insertTime;
  }

  public static EcclesiaResponseDTO fromEntity(Ecclesia ecclesia) {
    return EcclesiaResponseDTO.builder()
        .uid(ecclesia.getUid())
        .name(ecclesia.getName())
        .status(ecclesia.getStatus())
        .insertTime(ecclesia.getInsertTime())
        .build();
  }

}


