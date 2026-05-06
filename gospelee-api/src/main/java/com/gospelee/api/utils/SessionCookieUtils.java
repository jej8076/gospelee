package com.gospelee.api.utils;

import com.gospelee.api.properties.SessionCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class SessionCookieUtils {

  private final SessionCookieProperties properties;

  public SessionCookieUtils(SessionCookieProperties properties) {
    this.properties = properties;
  }

  /**
   * 세션 쿠키를 응답에 추가한다.
   */
  public void setSessionCookie(HttpServletResponse response, String sessionId) {
    String cookieValue = buildCookieHeader(sessionId, properties.getMaxAge());
    response.addHeader("Set-Cookie", cookieValue);
  }

  /**
   * 세션 쿠키를 만료시킨다 (값을 비우고 Max-Age=0).
   */
  public void clearSessionCookie(HttpServletResponse response) {
    String cookieValue = buildCookieHeader("", 0);
    response.addHeader("Set-Cookie", cookieValue);
  }

  /**
   * 요청에서 세션 ID를 추출한다.
   */
  public String extractSessionId(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (properties.getName().equals(cookie.getName())) {
        String value = cookie.getValue();
        return (value == null || value.isBlank()) ? null : value;
      }
    }
    return null;
  }

  private String buildCookieHeader(String value, long maxAgeSeconds) {
    StringBuilder sb = new StringBuilder();
    sb.append(properties.getName()).append('=').append(value);
    sb.append("; Path=").append(properties.getPath());
    sb.append("; Max-Age=").append(maxAgeSeconds);
    sb.append("; HttpOnly");
    if (properties.isSecure()) {
      sb.append("; Secure");
    }
    if (properties.getSameSite() != null && !properties.getSameSite().isBlank()) {
      sb.append("; SameSite=").append(properties.getSameSite());
    }
    if (properties.getDomain() != null && !properties.getDomain().isBlank()) {
      sb.append("; Domain=").append(properties.getDomain());
    }
    return sb.toString();
  }
}
