package com.gospelee.api.auth.jwt;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.account.AccountEcclesiaInfo;
import com.gospelee.api.dto.account.TokenDTO;
import com.gospelee.api.dto.auth.SessionData;
import com.gospelee.api.dto.common.ResponseDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.enums.AppType;
import com.gospelee.api.enums.Bearer;
import com.gospelee.api.enums.CustomHeader;
import com.gospelee.api.enums.ErrorResponseType;
import com.gospelee.api.enums.SocialLoginPlatform;
import com.gospelee.api.enums.TokenHeaders;
import com.gospelee.api.properties.AuthProperties;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.SessionService;
import com.gospelee.api.utils.IpUtils;
import com.gospelee.api.utils.SessionCookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import util.JsonUtils;

@Component
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final AuthProperties authProperties;
  private final AccountService accountService;
  private final SocialJwtProviderFactory providerFactory;
  private final SessionService sessionService;
  private final SessionCookieUtils sessionCookieUtils;
  private final String NONCE_CHECK_PATH = "/account/auth/success";

  public JwtAuthenticationFilter(AuthProperties authProperties, AccountService accountService,
      SocialJwtProviderFactory providerFactory, SessionService sessionService,
      SessionCookieUtils sessionCookieUtils) {
    this.authProperties = authProperties;
    this.accountService = accountService;
    this.providerFactory = providerFactory;
    this.sessionService = sessionService;
    this.sessionCookieUtils = sessionCookieUtils;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException, BadCredentialsException {

    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientIp = IpUtils.getClientIp(request);

    // 1. cookie 기반 세션 우선 확인 (admin 웹)
    String sessionId = sessionCookieUtils.extractSessionId(request);
    if (sessionId != null) {
      Optional<SessionData> sessionOpt = sessionService.get(sessionId);
      if (sessionOpt.isPresent()) {
        if (applySessionAuthenticationToContext(sessionOpt.get())) {
          filterChain.doFilter(request, response);
          return;
        }
      }
      // session ID는 있는데 Redis에 없거나 만료 → 401
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    // 2. cookie 없으면 기존 헤더 기반 로직 (모바일 앱)
    String socialLoginPlatform = request.getHeader(
        CustomHeader.SOCIAL_LOGIN_PLATFORM.getHeaderName());
    String appId = request.getHeader(CustomHeader.X_APP_IDENTIFIER.getHeaderName());
    boolean isWeb = AppType.PODO_WEB.getValue().equals(appId);

    String nonceCacheKey = null;
    if (NONCE_CHECK_PATH.equals(request.getServletPath())) {
      nonceCacheKey = request.getHeader(CustomHeader.X_ANONYMOUS_USER_ID.getHeaderName());
    }

    TokenDTO tokenDTO = TokenDTO.builder()
        .socialLoginPlatform(SocialLoginPlatform.of(socialLoginPlatform))
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
      applySuperAuthenticationToContext(tokenDTO);
      filterChain.doFilter(request, response);
      return;
    }

    SocialJwtProvider socialJwtProvider = providerFactory.getProvider(
        tokenDTO.getSocialLoginPlatform());

    if (socialJwtProvider == null) {
      log.warn("[JWT] unsupported platform: {}", tokenDTO.getSocialLoginPlatform());
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    JwtPayload jwtPayload = socialJwtProvider.getOIDCPayload(tokenDTO.getIdToken(), nonceCacheKey);

    if (ObjectUtils.isEmpty(jwtPayload)) {
      failResponse(response, ErrorResponseType.AUTH_103);
      return;
    }

    // 인증 주입
    Authentication authentication = applyAuthenticationToContext(socialJwtProvider, jwtPayload,
        tokenDTO);

    if (authentication == null) {
      failResponse(response, ErrorResponseType.AUTH_111, HttpStatus.GONE);
      return;
    }

    if (!(authentication.getPrincipal() instanceof AccountAuthDTO)) {
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

  private void applySuperAuthenticationToContext(TokenDTO tokenDTO) {
    Authentication authentication = getSuperAuthentication(tokenDTO);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  /**
   * 세션 데이터로부터 SecurityContext에 인증 정보 주입.
   * super 세션이면 super 계정 조회, 일반 세션이면 email로 계정 조회.
   *
   * @return 성공 시 true, 계정 조회 실패 등으로 실패 시 false
   */
  private boolean applySessionAuthenticationToContext(SessionData session) {
    Optional<AccountAuthDTO> accountOpt;
    if (session.isSuperUser()) {
      accountOpt = accountService.handleSuperUserAuthentication();
    } else {
      accountOpt = accountService.getAccountByEmail(session.getEmail())
          .map(account -> {
            AccountEcclesiaInfo ecclesiaInfo = accountService.resolveEcclesiaInfo(account);
            return AccountAuthDTO.builder()
                .uid(account.getUid())
                .email(account.getEmail())
                .name(account.getName())
                .phone(account.getPhone())
                .role(account.getRole())
                .ecclesiaUid(ecclesiaInfo.getEcclesiaUid())
                .ecclesiaStatus(ecclesiaInfo.getEcclesiaStatus())
                .pushToken(account.getPushToken())
                .idToken(session.getIdToken())
                .accessToken(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .socialLoginPlatform(session.getSocialLoginPlatform())
                .build();
          });
    }

    if (accountOpt.isEmpty()) {
      return false;
    }

    AccountAuthDTO account = accountOpt.get();
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(account, "", account.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return true;
  }

  private Authentication applyAuthenticationToContext(SocialJwtProvider socialJwtProvider,
      JwtPayload jwtPayload, TokenDTO tokenDTO) {
    Authentication authentication = socialJwtProvider.getAuthentication(jwtPayload,
        tokenDTO);
    if (authentication.getPrincipal() == null) {
      // TODO authentication 이 null인 경우는 회원이 있으나 탈퇴한 경우 뿐임, 코드만 봐서는 의도가 명확하지 않으므로 의도 명확히 해야함
      return null;
    }
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

  private UsernamePasswordAuthenticationToken getSuperAuthentication(TokenDTO tokenDTO) {
    Optional<AccountAuthDTO> accountAuthDTO = accountService.handleSuperUserAuthentication();
    return accountAuthDTO.map(account -> {
      UserDetails userDetails = AccountAuthDTO.builder()
          .uid(account.getUid())
          .email(account.getEmail())
          .name(account.getName())
          .role(account.getRole())
          .idToken(account.getIdToken())
          .accessToken(account.getAccessToken())
          .refreshToken(account.getRefreshToken())
          .ecclesiaUid(account.getEcclesiaUid())
          .pushToken(account.getPushToken())
          .ecclesiaStatus(account.getEcclesiaStatus())
          .socialLoginPlatform(tokenDTO.getSocialLoginPlatform())
          .build();
      return new UsernamePasswordAuthenticationToken(userDetails, tokenDTO.getIdToken(),
          userDetails.getAuthorities());
    }).orElseThrow(() -> new RuntimeException("account 불러오기 혹은 저장 실패"));
  }

}
