package com.gospelee.api.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.Jwts;
import java.net.http.HttpClient;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * 애플 토큰 엔드포인트(https://appleid.apple.com/auth/token) 호출.
 * 인증 코드 → refresh token 교환, refresh token → 새 id token 발급을 담당한다.
 * apple.team-id / key-id / private-key(.p8 내용) 가 설정되지 않으면 비활성(빈 결과)이다.
 */
@Slf4j
@Component
public class AppleAuthClient {

  private static final String TOKEN_URL = "https://appleid.apple.com/auth/token";
  private static final String AUDIENCE = "https://appleid.apple.com";
  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private final RestClient restClient;
  private final String clientId;
  private final String teamId;
  private final String keyId;
  private final String privateKeyPem;

  @Autowired
  public AppleAuthClient(RestClient.Builder builder,
      @Value("${apple.app-key}") String clientId,
      @Value("${apple.team-id:}") String teamId,
      @Value("${apple.key-id:}") String keyId,
      @Value("${apple.private-key:}") String privateKeyPem) {
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
        HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
    factory.setReadTimeout(TIMEOUT);
    this.restClient = builder.requestFactory(factory).build();
    this.clientId = clientId;
    this.teamId = teamId == null ? "" : teamId.trim();
    this.keyId = keyId == null ? "" : keyId.trim();
    this.privateKeyPem = privateKeyPem == null ? "" : privateKeyPem.trim();
  }

  public boolean isConfigured() {
    return !teamId.isEmpty() && !keyId.isEmpty() && !privateKeyPem.isEmpty();
  }

  /** 앱이 로그인 때 받은 authorizationCode 를 refresh token 으로 교환한다. */
  public Optional<AppleTokenResponse> exchangeAuthorizationCode(String authorizationCode) {
    MultiValueMap<String, String> form = baseForm();
    form.add("grant_type", "authorization_code");
    form.add("code", authorizationCode);
    return requestToken(form);
  }

  /** 저장해 둔 refresh token 으로 새 id token 을 받는다. */
  public Optional<AppleTokenResponse> refresh(String refreshToken) {
    MultiValueMap<String, String> form = baseForm();
    form.add("grant_type", "refresh_token");
    form.add("refresh_token", refreshToken);
    return requestToken(form);
  }

  private MultiValueMap<String, String> baseForm() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", clientId);
    form.add("client_secret", buildClientSecret());
    return form;
  }

  private Optional<AppleTokenResponse> requestToken(MultiValueMap<String, String> form) {
    if (!isConfigured()) {
      log.warn("[APPLE] apple.team-id/key-id/private-key 가 설정되지 않아 토큰 요청을 건너뜁니다.");
      return Optional.empty();
    }
    try {
      AppleTokenResponse response = restClient.post()
          .uri(TOKEN_URL)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .retrieve()
          .body(AppleTokenResponse.class);
      return Optional.ofNullable(response);
    } catch (Exception e) {
      // 코드/토큰 값은 로그에 남기지 않는다
      log.warn("[APPLE] 토큰 요청 실패: {}", e.getMessage());
      return Optional.empty();
    }
  }

  /** 애플이 요구하는 client_secret: .p8 키로 서명한 ES256 JWT (유효 10분) */
  String buildClientSecret() {
    Date now = new Date();
    return Jwts.builder()
        .header().keyId(keyId).and()
        .issuer(teamId)
        .subject(clientId)
        .audience().add(AUDIENCE).and()
        .issuedAt(now)
        .expiration(new Date(now.getTime() + 10 * 60 * 1000L))
        .signWith(parsePrivateKey(privateKeyPem), Jwts.SIG.ES256)
        .compact();
  }

  static PrivateKey parsePrivateKey(String pem) {
    try {
      // 환경 변수/yml 에서는 줄바꿈이 \n 문자열로 들어올 수 있다
      String body = pem.replace("\\n", "\n")
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replace("-----END PRIVATE KEY-----", "")
          .replaceAll("\\s", "");
      byte[] der = Base64.getDecoder().decode(body);
      return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(der));
    } catch (Exception e) {
      throw new IllegalStateException("apple.private-key 형식이 올바르지 않습니다.", e);
    }
  }

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AppleTokenResponse {

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("refresh_token")
    private String refreshToken;
  }
}
