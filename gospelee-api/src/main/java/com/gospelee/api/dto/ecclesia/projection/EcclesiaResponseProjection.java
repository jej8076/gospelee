package com.gospelee.api.dto.ecclesia.projection;

import java.time.LocalDateTime;

public interface EcclesiaResponseProjection {

  Long getEcclesiaUid();

  String getChurchIdentificationNumber();

  String getStatus();

  String getEcclesiaName();

  String getMasterAccountName();

  LocalDateTime getInsertTime();
}