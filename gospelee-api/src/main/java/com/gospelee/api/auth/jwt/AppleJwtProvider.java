package com.gospelee.api.auth.jwt;

import static util.Base64Util.decodeBase64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.dto.jwt.JwkDTO;
import com.gospelee.api.dto.jwt.JwkSetDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.enums.RedisCacheNames;
import com.gospelee.api.enums.SocialLoginPlatform;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.RedisCacheService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class AppleJwtProvider extends SocialJwtProvider {

  private final RedisCacheService redisCacheService;
  @Value("${apple.issuer}")
  private String APPLE_ISS;
  @Value("${apple.app-key}")
  private String APPLE_SERVICE_APP_KEY;

  public AppleJwtProvider(AccountService accountService, RedisCacheService redisCacheService) {
    super(accountService);
    this.redisCacheService = redisCacheService;
  }

  public SocialLoginPlatform getSupportedPlatform() {
    return SocialLoginPlatform.APPLE;
  }

  public JwtPayload getOIDCPayload(String token, String nonceCacheKey)
      throws JsonProcessingException {

    if (!validationIdToken(token, nonceCacheKey)) {
      log.error("token 유효성 검증 실패 [" + token + "]");
      return null;
    }

    // 애플 제공 공개키(캐싱)
    JwkSetDTO cachedJwkSet = redisCacheService.getApplePublicKeySet();
    Optional<String> kidOptional = getKid(token);

    // 예외 처리를 호출자 쪽으로 위임
    if (kidOptional.isEmpty()) {
      log.error("KID를 가져오지 못했습니다.");
      return null;
    }

    // kid가 서로 일치하는 데이터 가져옴
    JwkDTO jwkDTO = cachedJwkSet.getKeys().stream()
        .filter(o -> o.getKid().equals(kidOptional.get()))
        .findFirst()
        .orElseThrow();

    // parseSignedClaims으로 decode된 공개키와 token이 일치하는 지 확인
    try {
      Jws<Claims> claimsJws = Jwts.parser()
          .verifyWith(getRSAPublicKey(jwkDTO.getN(), jwkDTO.getE()))
          .build()
          .parseSignedClaims(getUnsignedToken(token));

      return JwtPayload.builder()
          .issuer(claimsJws.getPayload().getIssuer())
          .audience(claimsJws.getPayload().getAudience())
          .sub(claimsJws.getPayload().getSubject())
          .nickname(claimsJws.getPayload().get("nickname", String.class))
          .email(claimsJws.getPayload().get("email", String.class))
          .build();

    } catch (RestClientException e) {
      log.error("RestClient 요청 실패", e);
      return null;
    } catch (Exception e) {
      log.error("token 유효성 검증 실패 [{}]", token, e);
      return null;
    }
  }

  /**
   * 서명은 애플 공개키로 검증하되 만료(exp)는 허용한다. id token 갱신 요청 전용이며, 만료 시각이
   * 얼마나 지났는지는 호출하는 쪽에서 제한한다. (jjwt 는 서명 검증 후에 exp 를 검사한다)
   */
  public Optional<Claims> verifyIgnoringExpiry(String token) {
    try {
      Optional<String> kid = getKid(token);
      if (kid.isEmpty()) {
        return Optional.empty();
      }
      JwkDTO jwk = redisCacheService.getApplePublicKeySet().getKeys().stream()
          .filter(o -> o.getKid().equals(kid.get()))
          .findFirst()
          .orElseThrow();
      var parser = Jwts.parser().verifyWith(getRSAPublicKey(jwk.getN(), jwk.getE())).build();
      try {
        return Optional.of(parser.parseSignedClaims(getUnsignedToken(token)).getPayload());
      } catch (io.jsonwebtoken.ExpiredJwtException e) {
        return Optional.of(e.getClaims());
      }
    } catch (Exception e) {
      log.warn("[APPLE] 만료 허용 토큰 검증 실패: {}", e.getMessage());
      return Optional.empty();
    }
  }

  public String getIssuer() {
    return APPLE_ISS;
  }

  public String getAppKey() {
    return APPLE_SERVICE_APP_KEY;
  }

  private boolean validationIdToken(String idToken, String nonceCacheKey)
      throws JsonProcessingException {

    if (ObjectUtils.isEmpty(idToken)) {
      return false;
    }

    String[] idTokenArr = idToken.split("\\.");

    if (idTokenArr.length <= 1) {
      return false;
    }

    String payload = decodeBase64(idTokenArr[1]);

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map;

    map = mapper.readValue(payload, new TypeReference<HashMap<String, Object>>() {
    });

    String iss = String.valueOf(map.get("iss"));

    if (!APPLE_ISS.equals(iss)) {
      return false;
    }

    String aud = String.valueOf(map.get("aud"));
    if (!APPLE_SERVICE_APP_KEY.equals(aud)) {
      return false;
    }

    long currentUnixTime = System.currentTimeMillis();
    long exp = Long.valueOf((Integer) map.get("exp")) * 1000;

    if (currentUnixTime > exp) {
      log.error("토큰이 만료되었습니다. [token : " + idToken + "]");
      return false;
    }

    // nonce 검증
    if (nonceCacheKey != null) {
      String cachedNonce = redisCacheService.get(RedisCacheNames.NONCE, nonceCacheKey);
      String nonce = String.valueOf(map.get("nonce"));
      if (!nonce.equals(cachedNonce)) {
        log.error("일치하는 nonce값이 없습니다. [platform: {}, nonceCacheKey: {}, nonce: {}]", "apple",
            nonceCacheKey, nonce);
        return false;
      }
      // nonce 일회용 — 검증 성공 후 즉시 삭제하여 replay attack 방지
      redisCacheService.delete(RedisCacheNames.NONCE, nonceCacheKey);
    }

    return true;
  }


  private Optional<String> getKid(String token) {
    try {
      String[] idTokenArr = token.split("\\.");
      String header = decodeBase64(idTokenArr[0]);

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> map = mapper.readValue(header,
          new TypeReference<HashMap<String, Object>>() {
          });

      return Optional.ofNullable(String.valueOf(map.get("kid")));
    } catch (Exception e) {
      log.error("getKid 처리 중 예외 발생", e);
      return Optional.empty(); // 예외 발생 시 빈 Optional 반환
    }
  }

}
