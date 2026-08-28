package com.gospelee.api.exception;

import com.gospelee.api.enums.ErrorResponseType;
import lombok.Getter;

@Getter
public class GalleryException extends RuntimeException {

  private final ErrorResponseType errorResponseType;

  public GalleryException(ErrorResponseType errorResponseType) {
    super(errorResponseType.message());
    this.errorResponseType = errorResponseType;
  }

  public GalleryException(ErrorResponseType errorResponseType, String customMessage) {
    super(customMessage);
    this.errorResponseType = errorResponseType;
  }
}
