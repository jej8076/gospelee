package com.gospelee.api.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WellKnownControllerTest {

  private final WellKnownController controller = new WellKnownController();

  @Test
  void assetlinks_는_업로드키와_구글_앱서명키_지문을_모두_포함한다() {
    String json = controller.assetLinks();

    assertTrue(json.contains("\"package_name\": \"org.podo\""));
    // 직접 설치/테스트 배포 빌드(업로드 키)
    assertTrue(json.contains("66:F6:17:3C:64:E2:ED:25:A2:00:F4:43:55:00:12:B6:CA:9F:15:45:7F:BB:CD:97:F2:F7:A3:B9:7D:81:16:22"));
    // 구글 플레이 설치 빌드(앱 서명 키)
    assertTrue(json.contains("55:99:CF:99:85:DE:BD:56:61:E1:A3:0A:E9:C9:14:0A:23:39:16:DB:AD:F2:1B:AF:27:0A:14:71:5B:9F:0D:F8"));
  }
}
