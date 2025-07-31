package com.gospelee.api.enums;

public enum DeepLinkRouterPath {
  QR_SCANNER("/qr/scanner"),
  ;

  final private String path;

  DeepLinkRouterPath(String path) {
    this.path = path;
  }


  public String path() {
    return path;
  }
}
