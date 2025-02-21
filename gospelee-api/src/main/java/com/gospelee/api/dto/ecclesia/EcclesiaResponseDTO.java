package com.gospelee.api.dto.ecclesia;

import java.time.LocalDateTime;
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

}


