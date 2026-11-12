package com.gospelee.api.service.announcement;

import com.gospelee.api.enums.PushNotificationDataType;
import java.util.Map;

/**
 * 공지사항 푸시 알림 문구: 제목은 고정, 내용에는 공지 제목을 넣어 알림만 보고도 내용을 알 수 있게 한다.
 */
final class AnnouncementPushMessage {

  static final String TITLE = "교회 공지사항";
  static final String DEFAULT_MESSAGE = "공지사항을 확인해주세요.";
  private static final int MAX_SUBJECT_LENGTH = 60;

  private static final int MAX_MINISTRY_NAME_LENGTH = 20;

  private static final String ROUTE_PREFIX = "/ecclesia/announcement/";

  private AnnouncementPushMessage() {
  }

  /**
   * 알림을 눌렀을 때 앱이 열어야 할 공지 상세 화면 경로
   */
  static Map<String, String> routeData(Long announcementId) {
    return Map.of(PushNotificationDataType.ROUTE.lower(), ROUTE_PREFIX + announcementId);
  }

  /** 모임 공지 상세 경로. 앱이 어느 모임의 공지인지 알 수 있도록 ministryId 를 붙인다 */
  static Map<String, String> ministryRouteData(Long announcementId, Long ministryId) {
    return Map.of(PushNotificationDataType.ROUTE.lower(),
        ROUTE_PREFIX + announcementId + "?ministryId=" + ministryId);
  }

  /** 모임 공지 푸시 제목: [모임이름] 공지. 이름이 길면 줄인다 */
  static String ministryTitle(String ministryName) {
    String name = ministryName == null ? "" : ministryName.replaceAll("\\s+", " ").trim();
    if (name.length() > MAX_MINISTRY_NAME_LENGTH) {
      name = name.substring(0, MAX_MINISTRY_NAME_LENGTH) + "...";
    }
    return name.isEmpty() ? "모임 공지사항" : "[" + name + "] 공지사항";
  }

  static String message(String subject) {
    if (subject == null || subject.isBlank()) {
      return DEFAULT_MESSAGE;
    }
    // 줄바꿈은 공백으로 바꾸고, 너무 길면 말줄임 처리
    String oneLine = subject.replaceAll("\\s+", " ").trim();
    if (oneLine.length() > MAX_SUBJECT_LENGTH) {
      return oneLine.substring(0, MAX_SUBJECT_LENGTH) + "...";
    }
    return oneLine;
  }
}
