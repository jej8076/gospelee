package com.gospelee.api.exception;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.exception.dto.FirebaseMessagingClientResponse;

/**
 * https://firebase.google.com/docs/cloud-messaging/error-codes?hl=ko
 */
public class FirebaseMessagingClientException extends RuntimeException {

  private final String errorCode; // FirebaseMessagingException의 코드 등
  private final int httpStatus;   // 기본 매핑 HTTP 상태

  public FirebaseMessagingClientException(String message, String errorCode, int httpStatus,
      Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public int getHttpStatus() {
    return httpStatus;
  }

  public static FirebaseMessagingClientException from(FirebaseMessagingException e) {
    // 필요 시 MessagingErrorCode → HTTP 상태/메시지 매핑
    FirebaseMessagingClientResponse response = mapToHttpStatus(e);
    String code = e.getMessagingErrorCode() != null ? e.getMessagingErrorCode().name() : "UNKNOWN";
    String msg = e.getMessage();
    return new FirebaseMessagingClientException(
        response.getMessage() == null ? msg : response.getMessage(), code, response.getCode(), e);
  }

  private static FirebaseMessagingClientResponse mapToHttpStatus(FirebaseMessagingException e) {
    // 예시 매핑 (원하시는 규칙으로 수정)
    if (e.getMessagingErrorCode() == null) {
      return FirebaseMessagingClientResponse.builder()
          .code(500)
          .message(null)
          .build();
    }
    switch (e.getMessagingErrorCode()) {
      case INVALID_ARGUMENT:
        return FirebaseMessagingClientResponse.builder()
            .code(400)
            .message(null)
            .build();
      case THIRD_PARTY_AUTH_ERROR:
        return FirebaseMessagingClientResponse.builder()
            .code(401)
            .message("iOS 기기 또는 웹 푸시 등록을 타겟팅한 메시지를 보낼 수 없습니다. 개발 및 프로덕션 인증 정보의 유효성을 확인하세요.")
            .build();
      case UNREGISTERED:
        return FirebaseMessagingClientResponse.builder()
            .code(410)
            .message(null)
            .build();
      case QUOTA_EXCEEDED:
        return FirebaseMessagingClientResponse.builder()
            .code(429)
            .message(null)
            .build();
      case INTERNAL:
        return FirebaseMessagingClientResponse.builder()
            .code(502)
            .message(null)
            .build();
      case UNAVAILABLE:
        return FirebaseMessagingClientResponse.builder()
            .code(503)
            .message(null)
            .build();
      default:
        return FirebaseMessagingClientResponse.builder()
            .code(500)
            .message(null)
            .build();
    }
  }
}

