package com.gospelee.api.exception.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FirebaseMessagingClientResponse {

  private int code;
  private String message;

  @Builder
  public FirebaseMessagingClientResponse(int code, String message) {
    this.code = code;
    this.message = message;
  }
}
