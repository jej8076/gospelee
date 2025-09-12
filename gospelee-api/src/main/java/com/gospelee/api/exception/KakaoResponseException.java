package com.gospelee.api.exception;

public class KakaoResponseException extends RuntimeException {

  public KakaoResponseException() {
    super();
  }

  public KakaoResponseException(String s) {
    super(s);
  }

  public KakaoResponseException(String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
  }

}
