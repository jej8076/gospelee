package com.gospelee.api.service.announcement;

/**
 * 공지사항 푸시 알림 문구: 제목은 고정, 내용에는 공지 제목을 넣어 알림만 보고도 내용을 알 수 있게 한다.
 */
final class AnnouncementPushMessage {

  static final String TITLE = "교회 공지사항";
  static final String DEFAULT_MESSAGE = "공지사항을 확인해주세요.";
  private static final int MAX_SUBJECT_LENGTH = 60;

  private AnnouncementPushMessage() {
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
