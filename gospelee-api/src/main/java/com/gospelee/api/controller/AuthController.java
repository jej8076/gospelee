package com.gospelee.api.controller;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.auth.SessionData;
import com.gospelee.api.dto.auth.SuperLoginRequest;
import com.gospelee.api.dto.common.ResponseDTO;
import com.gospelee.api.enums.SocialLoginPlatform;
import com.gospelee.api.properties.AuthProperties;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.AppleAuthService;
import com.gospelee.api.service.ReviewLoginService;
import com.gospelee.api.utils.IpUtils;
import com.gospelee.api.dto.common.DataResponseDTO;
import com.gospelee.api.enums.Bearer;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestHeader;
import com.gospelee.api.service.SessionService;
import com.gospelee.api.utils.SessionCookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthProperties authProperties;
  private final AccountService accountService;
  private final AppleAuthService appleAuthService;
  private final ReviewLoginService reviewLoginService;
  private final SessionService sessionService;
  private final SessionCookieUtils sessionCookieUtils;

  /**
   * Super 계정 로그인. 256자 super-pass 검증 후 세션 발급 + Set-Cookie.
   */
  @PostMapping("/super")
  public ResponseEntity<Object> superLogin(
      @RequestBody SuperLoginRequest request,
      HttpServletResponse response) {

    if (request == null || request.getPassword() == null
        || !request.getPassword().equals(authProperties.getSuperPass())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ResponseDTO.builder().code("AUTH-103").message("Unauthorized").build());
    }

    Optional<AccountAuthDTO> superAccount = accountService.handleSuperUserAuthentication();
    if (superAccount.isEmpty()) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ResponseDTO.builder().code("AUTH-500").message("Super 계정 조회 실패").build());
    }

    AccountAuthDTO account = superAccount.get();
    SessionData sessionData = SessionData.builder()
        .accountUid(account.getUid())
        .email(account.getEmail())
        .socialLoginPlatform(SocialLoginPlatform.EMPTY)
        .superUser(true)
        .build();

    String sessionId = sessionService.create(sessionData);
    sessionCookieUtils.setSessionCookie(response, sessionId);

    log.info("[SUPERLOGIN] success email={}", account.getEmail());
    return ResponseEntity.ok(ResponseDTO.builder().code("100").message("성공").build());
  }

  /**
   * 모바일 앱 → admin 웹뷰 접근용 세션 발급.
   * JwtAuthenticationFilter에서 소셜 토큰 헤더로 인증 후 SESSION 쿠키 발급.
   */
  @PostMapping("/session")
  public ResponseEntity<Object> issueSession(
      @AuthenticationPrincipal AccountAuthDTO account,
      HttpServletResponse response) {

    if (account == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ResponseDTO.builder().code("AUTH-103").message("Unauthorized").build());
    }

    SessionData sessionData = SessionData.builder()
        .accountUid(account.getUid())
        .email(account.getEmail())
        .socialLoginPlatform(account.getSocialLoginPlatform())
        .idToken(account.getIdToken())
        .accessToken(account.getAccessToken())
        .refreshToken(account.getRefreshToken())
        .superUser(false)
        .build();

    String sessionId = sessionService.create(sessionData);
    sessionCookieUtils.setSessionCookie(response, sessionId);

    log.info("[SESSION] issued for email={}", account.getEmail());
    return ResponseEntity.ok(ResponseDTO.builder().code("100").message("성공").build());
  }

  /**
   * 애플 id token 갱신. id token 이 만료돼 인증 필터를 통과할 수 없으므로 제외 경로(/auth/apple/refresh)로
   * 두고, 만료된 토큰의 서명·발급자·앱 대상과 만료 후 경과 기간을 직접 검증한다.
   */
  @PostMapping("/apple/refresh")
  public ResponseEntity<Object> refreshAppleToken(
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    String prefix = Bearer.BEARER_SPACE.getValue();
    String expiredToken = authorization != null && authorization.startsWith(prefix)
        ? authorization.substring(prefix.length()) : null;

    return appleAuthService.refreshIdToken(expiredToken)
        .<ResponseEntity<Object>>map(idToken -> ResponseEntity.ok(
            DataResponseDTO.of("100", "성공", Map.of("idToken", idToken))))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ResponseDTO.builder().code("AUTH-103").message("Unauthorized").build()));
  }

  /**
   * 앱스토어 심사용 계정 로그인. ID/비밀번호를 서버에서 확인하고 심사용 토큰을 발급한다.
   * 인증 전 호출이라 제외 경로(/auth/review/login)로 두며, 실패가 반복되면 IP 별로 잠근다.
   */
  @PostMapping("/review/login")
  public ResponseEntity<Object> reviewLogin(@RequestBody ReviewLoginRequest request,
      HttpServletRequest httpRequest) {
    return reviewLoginService
        .login(request.id(), request.password(), IpUtils.getClientIp(httpRequest))
        .<ResponseEntity<Object>>map(token -> ResponseEntity.ok(
            DataResponseDTO.of("100", "성공", Map.of("token", token))))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ResponseDTO.builder().code("AUTH-102").message("Unauthorized").build()));
  }

  public record ReviewLoginRequest(String id, String password) {

  }

  /**
   * 로그아웃. 세션 무효화 + Cookie 만료.
   */
  @PostMapping("/logout")
  public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
    String sessionId = sessionCookieUtils.extractSessionId(request);
    if (sessionId != null) {
      sessionService.delete(sessionId);
    }
    sessionCookieUtils.clearSessionCookie(response);
    return ResponseEntity.ok(ResponseDTO.builder().code("100").message("성공").build());
  }
}
