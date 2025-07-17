package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EcclesiaInsertDTO {

  private String name;

  private String churchIdentificationNumber;

  private String telephone;

  @Builder
  public EcclesiaInsertDTO(String name, String churchIdentificationNumber, String telephone) {
    this.name = name;
    this.churchIdentificationNumber = churchIdentificationNumber;
    this.telephone = telephone;
  }
}


