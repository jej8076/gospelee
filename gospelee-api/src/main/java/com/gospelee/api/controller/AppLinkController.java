package com.gospelee.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-link")
public class AppLinkController {

  private static final String APP = "oog";
  private static final String API = "api";
  private static final String APP_LINK = "app-link";
  private static final String fallbackUrl = "https://teleport.oog.kr";

  /**
   * 딥링크 테스트 endpoint
   *
   * @param request
   * @param userAgent
   * @return
   */
  @GetMapping("/**")
  public ResponseEntity<String> appLink(HttpServletRequest request,
      @RequestHeader(value = "User-Agent", defaultValue = "") String userAgent) {

    if (!isIosOrAndroid(userAgent)) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.TEXT_HTML);
      return new ResponseEntity<>("", headers, HttpStatus.OK);
    }

    String uri = request.getRequestURI();
    String[] parts = uri.split("/");
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(APP + "://");

    boolean isFirst = true;
    for (String part : parts) {
      if (part.isBlank() || API.equals(part) | APP_LINK.equals(part)) {
        continue;
      }

      if (!isFirst) {
        stringBuilder.append("/");
      }
      stringBuilder.append(part);
      isFirst = false;
    }

    String appSchemeUrl = stringBuilder.toString();

    // HTML로 앱 호출 시도 후 fallback
    String html = browserHtml().formatted(fallbackUrl, appSchemeUrl);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_HTML);
    return new ResponseEntity<>(html, headers, HttpStatus.OK);
  }

  private boolean isIosOrAndroid(String userAgent) {
    if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
      return true;
    }

    if (userAgent.toLowerCase().contains("android")) {
      return true;
    }

    return false;
  }

  private String browserHtml() {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>앱 열기</title>
            <script type="text/javascript">
                window.onload = function() {
                    var now = new Date().getTime();
                    setTimeout(function() {
                        var elapsed = new Date().getTime() - now;
                        if (elapsed < 2000) {
                            window.location.href = '%s'; // fallback
                        }
                    }, 1000);
                    window.location.href = '%s'; // 앱 스킴 시도
                };
            </script>
        </head>
        <body>
            앱을 여는 중입니다...
        </body>
        </html>
        """;
  }
}
