package com.gospelee.api.service;

import static com.gospelee.api.utils.StringUtils.SCHEME_DELIMITER;
import static com.gospelee.api.utils.StringUtils.SLASH;

import com.gospelee.api.dto.applink.AppLinkDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppLinkServiceImpl implements AppLinkService {

  // TODO application.yml에서 설정하도록 뺄 것
  private static final String APP = "oog";
  private static final String API = "api";
  private static final String APP_LINK = "app-link";
  private static final String fallbackUrl = "https://teleport.oog.kr";

  @Override
  public AppLinkDTO makeAppUrl(HttpServletRequest request, String userAgent) {
    if (!isIosOrAndroid(userAgent)) {
      return AppLinkDTO.builder().build();
    }

    String uri = request.getRequestURI();
    String[] parts = uri.split(SLASH);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(APP + SCHEME_DELIMITER);

    boolean isFirst = true;
    for (String part : parts) {
      if (part.isBlank() || API.equals(part) | APP_LINK.equals(part)) {
        continue;
      }

      if (!isFirst) {
        stringBuilder.append(SLASH);
      }
      stringBuilder.append(part);
      isFirst = false;
    }
    String appUrl = stringBuilder.toString();
    return AppLinkDTO.builder()
        .appUrl(appUrl)
        .fallbackUrl(fallbackUrl)
        .build();
  }

  @Override
  public String makeRedirectHtml() {
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

  private boolean isIosOrAndroid(String userAgent) {
    if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
      return true;
    }

    if (userAgent.toLowerCase().contains("android")) {
      return true;
    }

    return false;
  }

}


