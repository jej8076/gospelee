package com.gospelee.api.enums;

public enum DeepLinkRouterPath {
  QR_SCANNER("/qr/scanner"),
  // 교회 가입 요청 승인 화면 (교회 관리자)
  ECCLESIA_JOIN_REQUESTS("/ecclesia/requests"),
  // 교회 탭
  CHURCH("/church"),
  ;

  final private String path;

  DeepLinkRouterPath(String path) {
    this.path = path;
  }


  public String path() {
    return path;
  }
}
