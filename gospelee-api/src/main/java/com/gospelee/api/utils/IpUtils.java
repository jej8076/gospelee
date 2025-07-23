package com.gospelee.api.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpUtils {

  private static final Set<String> LOCAL_IPS = Set.of("0:0:0:0:0:0:0:1", "127.0.0.1");

  private static final String[] HEADER_NAME = {
      "X-Gateway-Client-IP",
      "X-Forwarded-For",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_CLIENT_IP",
      "HTTP_X_FORWARDED_FOR"
  };

  public static String getClientIp(HttpServletRequest request) {

    for (String header : HEADER_NAME) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        // 여러 개의 IP가 있을 경우 headerNames의 첫 번째 배열 IP를 반환
        return ip.split(",")[0].trim();
      }
    }

    // 헤더에서 IP를 찾지 못했을 경우 request.getRemoteAddr() 사용
    String originIp = request.getRemoteAddr();

    if (LOCAL_IPS.contains(originIp)) {
      return "127.0.0.1";
    }

    return originIp;
  }
}
