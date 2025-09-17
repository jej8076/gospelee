package com.gospelee.api.exception;

public class MissingRequiredValueException extends RuntimeException {

  public MissingRequiredValueException() {
    super();
  }

  public MissingRequiredValueException(String s) {
    super(s);
  }

  public MissingRequiredValueException(String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
  }

}
