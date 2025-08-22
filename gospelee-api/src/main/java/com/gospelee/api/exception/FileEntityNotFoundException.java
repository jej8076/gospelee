package com.gospelee.api.exception;

public class FileEntityNotFoundException extends RuntimeException {

  public FileEntityNotFoundException() {
    super();
  }

  public FileEntityNotFoundException(String s) {
    super(s);
  }

  public FileEntityNotFoundException(String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
  }

}
