package com.gospelee.api.exception;

public class AnnouncementNotFoundException extends RuntimeException {

  public AnnouncementNotFoundException() {
    super();
  }

  public AnnouncementNotFoundException(String s) {
    super(s);
  }

  public AnnouncementNotFoundException(String messageFormat, Object... args) {
    super(String.format(messageFormat, args));
  }

}
