package com.gospelee.api.dto.applink;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppLinkDTO {

  private String appUrl;

  private String fallbackUrl;

  @Builder
  public AppLinkDTO(String appUrl, String fallbackUrl) {
    this.appUrl = appUrl;
    this.fallbackUrl = fallbackUrl;
  }
}


