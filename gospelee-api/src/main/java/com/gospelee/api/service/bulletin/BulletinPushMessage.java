package com.gospelee.api.service.bulletin;

import com.gospelee.api.enums.PushNotificationDataType;
import java.time.LocalDate;
import java.util.Map;

/** 주보 푸시 알림 문구: 제목은 고정, 내용에는 주보 제목(없으면 날짜)을 넣는다 */
final class BulletinPushMessage {

  static final String TITLE = "교회 주보";
  private static final int MAX_LENGTH = 60;

  /** 알림을 누르면 교회 탭을 연다 (주보 탭으로 바로 이동하는 것은 앱 쪽 확장이 필요) */
  private static final String ROUTE = "/church";

  private BulletinPushMessage() {
  }

  static Map<String, String> routeData() {
    return Map.of(PushNotificationDataType.ROUTE.lower(), ROUTE);
  }

  static String message(String title, LocalDate worshipDate) {
    if (title != null && !title.isBlank()) {
      String oneLine = title.replaceAll("\\s+", " ").trim();
      return oneLine.length() > MAX_LENGTH ? oneLine.substring(0, MAX_LENGTH) + "..." : oneLine;
    }
    return worshipDate.getMonthValue() + "월 " + worshipDate.getDayOfMonth() + "일 주보가 올라왔어요.";
  }
}
