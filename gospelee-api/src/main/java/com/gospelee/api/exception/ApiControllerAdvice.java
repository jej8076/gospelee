package com.gospelee.api.exception;

import com.gospelee.api.dto.common.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.gospelee.api")
@RequiredArgsConstructor
@Slf4j
public class ApiControllerAdvice {

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseDTO AccessDeniedException(HttpServletRequest request, Exception ex) {
    log.error("[AccessDeniedException] ip={}, message={}", request.getRemoteAddr(), ex.getMessage(),
        ex);

    return ResponseDTO.of("400", ex.getMessage());
  }

  @ExceptionHandler(MissingRequiredValueException.class)
  public ResponseDTO MissingRequiredValueException(HttpServletRequest request, Exception ex) {
    log.error("[MissingRequiredValueException] ip={}, message={}", request.getRemoteAddr(),
        ex.getMessage(), ex);

    return ResponseDTO.of("400", ex.getMessage());
  }

  @ExceptionHandler(KakaoResponseException.class)
  public ResponseDTO KakaoResponseException(HttpServletRequest request, Exception ex) {
    log.error("[KakaoResponseException] ip={}, message={}", request.getRemoteAddr(),
        ex.getMessage(), ex);

    return ResponseDTO.of("400", ex.getMessage());
  }

  @ExceptionHandler(FirebaseMessagingClientException.class)
  public ResponseDTO FirebaseMessagingClientException(HttpServletRequest request,
      FirebaseMessagingClientException ex) {
    // 원인(FCM 예외)까지 함께 로그
    log.error("[FirebaseMessagingClientException] ip={}, status={}, code={}, message={}",
        request.getRemoteAddr(), ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), ex);

    return ResponseEntity
        .status(ex.getHttpStatus())
        .body(ResponseDTO.of(String.valueOf(ex.getHttpStatus()), ex.getMessage())).getBody();
  }

  @ExceptionHandler(Exception.class)
  public ResponseDTO unHandleExceptions(HttpServletRequest request, Exception ex) {
    log.error("[Unhandled Exception] ip={}, message={}", request.getRemoteAddr(), ex.getMessage(),
        ex);

    return ResponseDTO.of("400", ex.getMessage());
  }
}
