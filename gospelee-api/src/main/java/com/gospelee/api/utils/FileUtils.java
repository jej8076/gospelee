package com.gospelee.api.utils;

import java.io.File;
import java.time.LocalDate;

public class FileUtils {

  /**
   * 오늘 날짜를 yyyy/MM/dd 형식으로 변환한 경로 문자열을 반환한다.
   *
   * @return 오늘 날짜 경로 (예: "2025/08/22" on Unix, "2025\08\22" on Windows)
   */
  public static String makeTodayPath() {
    LocalDate today = LocalDate.now();
    return File.separator
        + today.getYear()
        + File.separator
        + String.format("%02d", today.getMonthValue())
        + File.separator
        + String.format("%02d", today.getDayOfMonth());
  }
}

