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
      List.of("");
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

  // EXCLUDE_URL과 동일한 요청이 들어왔을 경우, 현재 필터를 진행하지 않고 다음 필터 진행
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return EXCLUDE_SERVLET_PATH_LIST.stream()
        .anyMatch(exclude -> exclude.equalsIgnoreCase(request.getServletPath()));
  }

  private void setAuthenticationToContext(JwtPayload jwtPayload, String idToken) {
    Authentication authentication = jwtOIDCProvider.getAuthentication(jwtPayload, idToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

}
