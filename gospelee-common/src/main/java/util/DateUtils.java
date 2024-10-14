package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

  private static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd HH:mm:ss");

  private static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd");


  // LocalDateTime -> String(yyyy-MM-dd HH:mm:ss)
  public static String timeToStringSec(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(FORMATTER_TIME);
  }

  // LocalDateTime -> String(yyyy-MM-dd)
  public static String timeToStringDate(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(FORMATTER_DATE);
  }

}
