package com.gospelee.api.exception;

public class SuperAccountException extends RuntimeException {

  public SuperAccountException() {
    super();
  }

  public SuperAccountException(String s) {
    super(s);
  }

  public SuperAccountException(String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
  }

}
