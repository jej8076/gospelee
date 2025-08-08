package com.gospelee.api.dto.ecclesia;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EcclesiaUpdateDTO {

  private Long ecclesiaUid;
  private String status;
  private String seniorPastorName;
  private String churchAddress;
}


