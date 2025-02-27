package com.gospelee.api.exception;

public class EcclesiaException extends RuntimeException {

  public EcclesiaException() {
    super();
  }

  public EcclesiaException(String s) {
    super(s);
  }

  public EcclesiaException(String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
  }

}
