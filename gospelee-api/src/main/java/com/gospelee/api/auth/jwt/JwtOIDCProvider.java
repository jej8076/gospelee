package com.gospelee.api.auth.jwt;

import static util.Base64Util.decodeBase64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.dto.jwt.JwkDTO;
import com.gospelee.api.dto.jwt.JwkSetDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.service.RedisCacheService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.JwkSet;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtOIDCProvider {

  private static final String KID = "kid";
  @Value("${kakao.issuer}")
  private String KAKAO_ISS;
  @Value("${kakao.app-key}")
  private String KAKAO_SERVICE_APP_KEY;

  private RedisCacheService redisCacheService;

  public JwtOIDCProvider(RedisCacheService redisCacheService) {
    this.redisCacheService = redisCacheService;
  }

  public JwtPayload getOIDCPayload(String token)
      throws JsonProcessingException {

    if (!validationIdToken(token)) {
      throw new RuntimeException("token 유효성 검증 실패 [" + token + "]");
    }

    // 카카오 제공 공개키(캐싱)
    JwkSetDTO cachedJwkSet = redisCacheService.getPublicKeySet();

    String kid = getKid(token);

    // kid가 서로 일치하는 데이터 가져옴
    JwkDTO jwkDTO = cachedJwkSet.getKeys().stream()
        .filter(o -> o.getKid().equals(kid))
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
    } catch (ExpiredJwtException e) {
      throw new IllegalArgumentException("만료된 Id Token 입니다.");
    } catch (Exception e) {
      throw new IllegalArgumentException("잘못된 Id Token 입니다.");
    }

  }

  public String getKidFromUnsignedIdToken(String token, String iss, String aud,
      JwkSet publicJwkSet) {
    return (String) getUnsignedClaims(token, iss, aud, publicJwkSet).getHeader().get(KID);
  }

  private Jwt<Header, Claims> getUnsignedClaims(String token, String iss, String aud,
      JwkSet publicJwkSet) {
    try {
      return Jwts.parser()
          .requireIssuer(iss)
          .requireAudience(aud)
          .build()
          .parseUnsecuredClaims(getUnsignedToken(token));
    } catch (ExpiredJwtException e) {
      throw new IllegalArgumentException("만료된 Id Token 입니다.");
    } catch (Exception e) {
      throw new IllegalArgumentException("잘못된 Id Token 입니다.");
    }
  }

  private CharSequence getUnsignedToken(String token) {
    String[] splitToken = token.split("\\.");
    if (splitToken.length != 3) {
      throw new IllegalArgumentException("잘못된 Id Token 입니다.");
    }
    return splitToken[0] + "." + splitToken[1] + "." + splitToken[2];
  }

  private boolean validationIdToken(String idToken) throws JsonProcessingException {

    String[] idTokenArr = idToken.split("\\.");

    String payload = decodeBase64(idTokenArr[1]);

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map;

    map = mapper.readValue(payload, new TypeReference<HashMap<String, Object>>() {
    });

    String iss = String.valueOf(map.get("iss"));

    if (!KAKAO_ISS.equals(iss)) {
      return false;
    }

    String aud = String.valueOf(map.get("aud"));
    if (!KAKAO_SERVICE_APP_KEY.equals(aud)) {
      return false;
    }

    long currentUnixTime = System.currentTimeMillis();
    long exp = Long.valueOf((Integer) map.get("exp")) * 1000;

    if (currentUnixTime > exp) {
      return false;
    }

    // TODO jej8076 nonce 값(카카오 로그인 요청 시 전달한 값과 일치하는지) 확인 필요

    return true;
  }

  private PublicKey getRSAPublicKey(String modulus, String exponent)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
    byte[] decodeE = Base64.getUrlDecoder().decode(exponent);
    BigInteger n = new BigInteger(1, decodeN);
    BigInteger e = new BigInteger(1, decodeE);

    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
    return keyFactory.generatePublic(keySpec);
  }

  private String getKid(String token) throws JsonProcessingException {
    String[] idTokenArr = token.split("\\.");
    String header = decodeBase64(idTokenArr[0]);
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map;

    map = mapper.readValue(header, new TypeReference<HashMap<String, Object>>() {
    });

    return String.valueOf(map.get("kid"));
  }

}