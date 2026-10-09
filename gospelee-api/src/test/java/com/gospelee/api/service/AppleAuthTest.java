package com.gospelee.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.auth.jwt.AppleJwtProvider;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AppleAuthToken;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.account.AppleAuthTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class AppleAuthTest {

  private static final String APP = "kr.po-do.app";
  private static final String ISS = "https://appleid.apple.com";
  private static final long DAY = 24 * 60 * 60 * 1000L;

  private AppleAuthClient client;
  private AppleJwtProvider provider;
  private AppleAuthTokenRepository tokens;
  private AccountRepository accounts;
  private AppleAuthService service;

  @BeforeEach
  void setUp() {
    client = mock(AppleAuthClient.class);
    provider = mock(AppleJwtProvider.class);
    tokens = mock(AppleAuthTokenRepository.class);
    accounts = mock(AccountRepository.class);
    when(provider.getIssuer()).thenReturn(ISS);
    when(provider.getAppKey()).thenReturn(APP);
    service = new AppleAuthService(client, provider, tokens, accounts, 60);
  }

  private Claims claims(String iss, String aud, long expiredDaysAgo, String email) {
    return Jwts.claims().issuer(iss).audience().add(aud).and()
        .expiration(new Date(System.currentTimeMillis() - expiredDaysAgo * DAY))
        .add("email", email).build();
  }

  private void givenAccount(Yn leave) {
    Account account = Account.builder().uid(7L).email("a@b.c").leaveYn(leave).build();
    when(accounts.findByEmail("a@b.c")).thenReturn(Optional.of(account));
  }

  private AppleAuthClient.AppleTokenResponse response(String idToken) {
    AppleAuthClient.AppleTokenResponse r = mock(AppleAuthClient.AppleTokenResponse.class);
    when(r.getIdToken()).thenReturn(idToken);
    return r;
  }

  @Test
  void 클라이언트_시크릿은_p8_키로_ES256_서명된다() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
    gen.initialize(new ECGenParameterSpec("secp256r1"));
    KeyPair pair = gen.generateKeyPair();
    String pem = "-----BEGIN PRIVATE KEY-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pair.getPrivate().getEncoded())
        + "\n-----END PRIVATE KEY-----";

    AppleAuthClient real = new AppleAuthClient(RestClient.builder(), APP, "TEAM", "KEY1",
        pem.replace("\n", "\\n")); // yml 에서 \n 문자열로 들어오는 경우도 허용

    String secret = real.buildClientSecret();
    Claims claims = Jwts.parser().verifyWith(pair.getPublic()).build()
        .parseSignedClaims(secret).getPayload();

    assertThat(claims.getIssuer()).isEqualTo("TEAM");
    assertThat(claims.getSubject()).isEqualTo(APP);
    assertThat(claims.getAudience()).contains(ISS);
    assertThat(real.isConfigured()).isTrue();
  }

  @Test
  void 만료된_토큰이어도_저장된_refresh_token으로_새_토큰을_받는다() {
    when(provider.verifyIgnoringExpiry("old")).thenReturn(
        Optional.of(claims(ISS, APP, 1, "a@b.c")));
    givenAccount(Yn.N);
    when(tokens.findById(7L)).thenReturn(Optional.of(new AppleAuthToken(7L, "r1")));
    AppleAuthClient.AppleTokenResponse refreshed = response("new-id-token");
    when(client.refresh("r1")).thenReturn(Optional.of(refreshed));

    assertThat(service.refreshIdToken("old")).contains("new-id-token");
  }

  @Test
  void 오래전에_만료된_토큰은_거부한다() {
    when(provider.verifyIgnoringExpiry("old")).thenReturn(
        Optional.of(claims(ISS, APP, 61, "a@b.c")));

    assertThat(service.refreshIdToken("old")).isEmpty();
    verify(client, never()).refresh(any());
  }

  @Test
  void 서명검증_실패_발급자_또는_앱이_다르면_거부한다() {
    when(provider.verifyIgnoringExpiry("bad")).thenReturn(Optional.empty());
    when(provider.verifyIgnoringExpiry("other-iss")).thenReturn(
        Optional.of(claims("https://evil", APP, 1, "a@b.c")));
    when(provider.verifyIgnoringExpiry("other-app")).thenReturn(
        Optional.of(claims(ISS, "other.app", 1, "a@b.c")));

    assertThat(service.refreshIdToken("bad")).isEmpty();
    assertThat(service.refreshIdToken("other-iss")).isEmpty();
    assertThat(service.refreshIdToken("other-app")).isEmpty();
    assertThat(service.refreshIdToken(null)).isEmpty();
    verify(client, never()).refresh(any());
  }

  @Test
  void 탈퇴한_계정이거나_저장된_refresh_token이_없으면_거부한다() {
    when(provider.verifyIgnoringExpiry("old")).thenReturn(
        Optional.of(claims(ISS, APP, 1, "a@b.c")));

    givenAccount(Yn.Y);
    assertThat(service.refreshIdToken("old")).isEmpty();

    givenAccount(Yn.N);
    when(tokens.findById(7L)).thenReturn(Optional.empty());
    assertThat(service.refreshIdToken("old")).isEmpty();
    verify(client, never()).refresh(any());
  }

  @Test
  void 인증코드를_교환해_refresh_token을_저장하고_실패해도_예외를_던지지_않는다() {
    AppleAuthClient.AppleTokenResponse r = mock(AppleAuthClient.AppleTokenResponse.class);
    when(r.getRefreshToken()).thenReturn("r2");
    when(client.exchangeAuthorizationCode("code")).thenReturn(Optional.of(r));
    when(tokens.findById(7L)).thenReturn(Optional.empty());

    service.saveRefreshToken(7L, "code");
    verify(tokens).save(any(AppleAuthToken.class));

    when(client.exchangeAuthorizationCode("boom")).thenThrow(new RuntimeException("x"));
    service.saveRefreshToken(7L, "boom"); // 예외 없음
    service.saveRefreshToken(7L, null);
  }
}
