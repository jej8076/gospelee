package com.gospelee.api.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

  public static LocalDateTime now() {
    return LocalDateTime.now(KOREA_ZONE_ID);
  }

  public static String nowInKoreaFormatted() {
    ZonedDateTime nowInKorea = ZonedDateTime.now(KOREA_ZONE_ID);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return nowInKorea.format(formatter);
  }
}
