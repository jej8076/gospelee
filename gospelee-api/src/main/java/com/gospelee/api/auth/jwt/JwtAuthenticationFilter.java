package com.gospelee.api.auth.jwt;

import com.gospelee.api.dto.common.ResponseDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.enums.ErrorResponseType;
import com.gospelee.api.utils.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import util.JsonUtils;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final String AUTH_HEADER = "Authorization";
  private final String BEARER = "Bearer ";

  // 인증에서 제외할 url
  private static final List<String> EXCLUDE_SERVLET_PATH_LIST =
      List.of(
          "/bible/*",
          "/account/qr/enter",
          "/account/qr/check"
      );
  private final JwtOIDCProvider jwtOIDCProvider;

  public JwtAuthenticationFilter(JwtOIDCProvider jwtOIDCProvider) {
    this.jwtOIDCProvider = jwtOIDCProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException, BadCredentialsException {
    String idToken = request.getHeader(AUTH_HEADER);

    if (!idToken.startsWith(BEARER)) {
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    idToken = idToken.replace(BEARER, "");
    JwtPayload jwtPayload = jwtOIDCProvider.getOIDCPayload(idToken);

    if (ObjectUtils.isEmpty(jwtPayload)) {
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    setAuthenticationToContext(jwtPayload, idToken);

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

  /**
   * 쿠키는 next.js 에서 비우도록 함
   *
   * @param response
   * @param errorResponseType
   */
  private void failResponse(HttpServletResponse response, ErrorResponseType errorResponseType) {

    int status = HttpStatus.UNAUTHORIZED.value();

    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    try {
      // 현재 스레드의 인증을 비움
      SecurityContextHolder.clearContext();

      ResponseDTO responseDTO = ResponseDTO.builder()
          .status(status)
          .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
          .message(errorResponseType.message())
          .build();
      String json = JsonUtils.toStringFromObject(responseDTO);

      response.getWriter().write(json);
    } catch (Exception e) {
      logger.debug(e.getMessage());
    }
  }
}
