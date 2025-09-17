package com.gospelee.api.exception;

import com.gospelee.api.dto.common.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @ExceptionHandler(Exception.class)
  public ResponseDTO unHandleExceptions(HttpServletRequest request, Exception ex) {
    log.error("[Unhandled Exception] ip={}, message={}", request.getRemoteAddr(), ex.getMessage(),
        ex);

    return ResponseDTO.of("400", ex.getMessage());
  }
}
