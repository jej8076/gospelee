package com.gospelee.api.auth.jwt;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.account.TokenDTO;
import com.gospelee.api.dto.common.ResponseDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.enums.AppType;
import com.gospelee.api.enums.Bearer;
import com.gospelee.api.enums.CustomHeader;
import com.gospelee.api.enums.ErrorResponseType;
import com.gospelee.api.enums.TokenHeaders;
import com.gospelee.api.properties.AuthProperties;
import com.gospelee.api.utils.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import util.JsonUtils;

@Component
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtOIDCProvider jwtOIDCProvider;
  private final AuthProperties authProperties;
  private final String NONCE_CHECK_PATH = "/account/auth/success";

  public JwtAuthenticationFilter(JwtOIDCProvider jwtOIDCProvider, AuthProperties authProperties) {
    this.jwtOIDCProvider = jwtOIDCProvider;
    this.authProperties = authProperties;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException, BadCredentialsException {

    String clientIp = IpUtils.getClientIp(request);
    String appId = request.getHeader(CustomHeader.X_APP_IDENTIFIER.getHeaderName());
    boolean isWeb = AppType.OOG_WEB.getValue().equals(appId);

    String nonceCacheKey = null;
    if (NONCE_CHECK_PATH.equals(request.getServletPath())) {
      nonceCacheKey = request.getHeader(CustomHeader.X_ANONYMOUS_USER_ID.getHeaderName());
    }

    System.out.println("request.getServletPath() = " + request.getServletPath());

    TokenDTO tokenDTO = TokenDTO.builder()
        .idToken(request.getHeader(TokenHeaders.AUTHORIZATION.getValue()))
        .accessToken(request.getHeader(TokenHeaders.SOCIAL_ACCESS_TOKEN.getValue()))
        .refreshToken(request.getHeader(TokenHeaders.SOCIAL_REFRESH_TOKEN.getValue()))
        .build();

    if (tokenDTO.getIdToken() == null || !tokenDTO.getIdToken()
        .startsWith(Bearer.BEARER_SPACE.getValue())) {
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    tokenDTO.removeBearerIdToken();

    if (isWeb && tokenDTO.getIdToken().equals(authProperties.getSuperPass())) {
      log.info("[SUPERLOGIN] clientIp:{}", clientIp);
      JwtPayload emptyPayload = JwtPayload.builder().build();
      setAuthenticationToContext(emptyPayload, tokenDTO);
      filterChain.doFilter(request, response);
      return;
    }

    if (tokenDTO.getAccessToken() == null) {
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    JwtPayload jwtPayload = jwtOIDCProvider.getOIDCPayload(tokenDTO.getIdToken(), nonceCacheKey);

    if (ObjectUtils.isEmpty(jwtPayload)) {
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    // 인증 주입
    Authentication authentication = setAuthenticationToContext(jwtPayload, tokenDTO);

    if (authentication == null || !(authentication.getPrincipal() instanceof AccountAuthDTO)) {
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return authProperties.getExcludePaths().stream()
        .anyMatch(path -> path.contains("*") ? request.getServletPath()
            .startsWith(path.split("\\/\\*")[0]) : path.equals(request.getServletPath()));
  }

  private boolean isAllowAndPendingPath(String requestURI) {
    return authProperties.getAllowPendingPaths().stream()
        .anyMatch(path -> path.contains("*") ? requestURI
            .startsWith(path.split("\\/\\*")[0]) : path.equals(requestURI));
  }

  private Authentication setAuthenticationToContext(JwtPayload jwtPayload, TokenDTO tokenDTO) {

    Authentication authentication = jwtOIDCProvider.getAuthentication(jwtPayload, tokenDTO);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return authentication;
  }

  /**
   * 쿠키는 next.js 에서 비우도록 함
   *
   * @param response
   * @param errorResponseType
   */
  private void failResponse(HttpServletResponse response, ErrorResponseType errorResponseType) {
    failResponse(response, errorResponseType, HttpStatus.UNAUTHORIZED);
  }

  private void failResponse(HttpServletResponse response, ErrorResponseType errorResponseType,
      HttpStatus httpStatus) {
    int status = httpStatus.value();

    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    try {
      // 현재 스레드의 인증을 비움
      SecurityContextHolder.clearContext();

      ResponseDTO responseDTO = ResponseDTO.builder()
          .code(errorResponseType.code())
          .message(httpStatus.getReasonPhrase())
          .build();
      String json = JsonUtils.toStringFromObject(responseDTO);

      response.getWriter().write(json);
    } catch (Exception e) {
      logger.debug(e.getMessage());
    }
  }
}
