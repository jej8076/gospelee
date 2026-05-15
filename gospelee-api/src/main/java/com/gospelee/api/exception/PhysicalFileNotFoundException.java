package com.gospelee.api.exception;

public class PhysicalFileNotFoundException extends RuntimeException {

  public PhysicalFileNotFoundException() {
    super();
  }

  public PhysicalFileNotFoundException(String s) {
    super(s);
  }

  public PhysicalFileNotFoundException(String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
  }

}
