package com.gospelee.api.utils;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

  public static final String AUTH_HEADER = "id_token";
  private static CookieUtils instance;

  @Value("${auth.cookie.path}")
  private String cookiePath;
  @Value("#{${auth.max-age}}")
  private long authMaxAge;
  @Value("${auth.cookie.domains}")
  private List<String> cookieDomains;

  public static Long getAuthCookieMaxAge() {
    return instance.authMaxAge;
  }

  public static Cookie makeCookie(String token, String reqDomain) {
    Optional<String> domain = instance.cookieDomains.stream().filter(reqDomain::equals).findFirst();
    String domainValue = domain.orElse(null);
    if (domainValue == null) {
      return null;
    }
    Cookie cookie = new Cookie(AUTH_HEADER, token);
    cookie.setPath(instance.cookiePath);
    cookie.setMaxAge((int) instance.authMaxAge);
    cookie.setDomain(domainValue);
    return cookie;
  }

  public static Cookie expireCookie(String reqDomain) {
    Optional<String> domain = instance.cookieDomains.stream().filter(reqDomain::equals).findFirst();
    String domainValue = domain.orElse(null);
    if (domainValue == null) {
      return null;
    }
    Cookie cookie = new Cookie(AUTH_HEADER, "");
    cookie.setPath(instance.cookiePath);
    cookie.setMaxAge(0);
    cookie.setDomain(domainValue);
    return cookie;
  }

  @PostConstruct
  public void init() {
    instance = this;
  }

}
