package com.gospelee.api.exception;

public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException() {
    super();
  }

  public AccountNotFoundException(String s) {
    super(s);
  }

  public AccountNotFoundException(String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
  }

}
