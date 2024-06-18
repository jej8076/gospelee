package com.gospelee.api.auth.jwt;

import com.gospelee.api.dto.jwt.JwtPayload;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  // 인증에서 제외할 url
  private static final List<String> EXCLUDE_SERVLET_PATH_LIST =
      List.of(
          "/bible/*",
          "/account/send/noti",
          "/account/qr/enter"
      );
  private final JwtOIDCProvider jwtOIDCProvider;

  public JwtAuthenticationFilter(JwtOIDCProvider jwtOIDCProvider) {
    this.jwtOIDCProvider = jwtOIDCProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String idToken = request.getHeader("id_token");

    JwtPayload jwtPayload = jwtOIDCProvider.getOIDCPayload(idToken);

    if (!ObjectUtils.isEmpty(jwtPayload)) {
      setAuthenticationToContext(jwtPayload, idToken);
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return EXCLUDE_SERVLET_PATH_LIST.stream()
        .anyMatch(path -> path.contains("*") ? request.getServletPath()
            .startsWith(path.split("\\/\\*")[0]) : path.equals(request.getServletPath()));
  }

  private void setAuthenticationToContext(JwtPayload jwtPayload, String idToken) {
    Authentication authentication = jwtOIDCProvider.getAuthentication(jwtPayload, idToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

}
