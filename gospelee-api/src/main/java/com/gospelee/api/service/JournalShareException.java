package com.gospelee.api.service;

import lombok.Getter;

/** 묵상 공유 처리 중 사용자에게 코드로 전달해야 하는 실패 */
@Getter
public class JournalShareException extends RuntimeException {

  public static final String NICKNAME_REQUIRED = "NICKNAME_REQUIRED";
  public static final String NOT_FOUND = "404";
  public static final String FORBIDDEN = "403";
  public static final String INVALID = "400";
  public static final String DUPLICATE = "409";

  private final String code;

  public JournalShareException(String code, String message) {
    super(message);
    this.code = code;
  }
}
