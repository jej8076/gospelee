package com.gospelee.api.auth.jwt;

import static util.Base64Util.decodeBase64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.dto.jwt.Jwk;
import com.gospelee.api.dto.jwt.JwkSet;
import com.gospelee.api.dto.jwt.JwtPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JwtOIDCProvider {

  private static final String KID = "kid";
  @Value("${kakao.issuer}")
  private String KAKAO_ISS;
  @Value("${kakao.app-key}")
  private String KAKAO_SERVICE_APP_KEY;

  public JwtPayload getOIDCPayload(String token) throws JsonProcessingException {

    if (!validationIdToken(token)) {
      throw new RuntimeException("token 유효성 검증 실패 [" + token + "]");
    }

    // 공개키
    JwkSet cachedJwkSet = getPublicKeySet();

    // 임시코드
    String[] idTokenArr = token.split("\\.");
    String header = decodeBase64(idTokenArr[0]);
    JwkSet jwkSet = new JwkSet();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map;

    map = mapper.readValue(header, new TypeReference<HashMap<String, Object>>() {
    });

    String kid = String.valueOf(map.get("kid"));

    // 비교
    Jwk jwk =
        cachedJwkSet.getKeys().stream()
            .filter(o -> o.getKid().equals(kid))
            .findFirst()
            .orElseThrow();

    Jwt<Header, Claims> jwtHeaderClames = Jwts.parser()
        .requireIssuer(KAKAO_ISS)
        .requireAudience(KAKAO_SERVICE_APP_KEY)
        .verifyWith((PublicKey) jwk)
        .build()
        .parseUnsecuredClaims(getUnsignedToken(token));

//    Jwt<Header, Claims> unSignedClaim = getUnsignedClaims(token, KAKAO_ISS, KAKAO_SERVICE_APP_KEY,
//        publicJwkSet);

//    String kid = getKidFromUnsignedIdToken(token, KAKAO_ISS, KAKAO_SERVICE_APP_KEY, jwkSet);
    JwtPayload jwtPayload = getPayloadFromIdToken(token, kid, jwk);

    return jwtPayload;
  }

  private JwkSet getPublicKey() {
    RestClient restClient = RestClient.builder()
        .baseUrl("https://kauth.kakao.com")
        .build();

    return restClient.get()
        .uri("/.well-known/jwks.json")
        .retrieve()
        .body(JwkSet.class);
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
    return splitToken[0] + "." + splitToken[1] + ".";
  }

  private Jws<Claims> getPayloadClaims(String token, String modulus, String exponent) {
    try {
      token = token.replace("—", "--");
      return Jwts.parser()
          .verifyWith(getRSAPublicKey(modulus, exponent))
          .build()
          .parseSignedClaims(token);
    } catch (ExpiredJwtException e) {
      throw new IllegalArgumentException("만료된 Id Token 입니다.");
    } catch (Exception e) {
      throw new IllegalArgumentException("잘못된 Id Token 입니다.");
    }
  }

  public JwtPayload getOIDCTokenBody(String token, String modulus, String exponent) {
    Claims body = getPayloadClaims(token, modulus, exponent).getBody();
    return JwtPayload.builder()
        .issuer(body.getIssuer())
        .audience(body.getAudience())
        .subject(body.getSubject())
        .email(body.get("email", String.class))
        .name(body.get("name", String.class))
        .nickname(body.get("nickname", String.class))
        .pickture(body.get("pickture", String.class))
        .build();
  }

  public JwtPayload getPayloadFromIdToken(String token, String kid, Jwk jwk) {
    // kid 가져오기
//    String kid = getKidFromUnsignedIdToken(token, iss, aud);
//    Jwk jwk =
//        jwkSet.getKeys().stream()
//            .filter(o -> o.getKid().equals(kid))
//            .findFirst()
//            .orElseThrow();
    return getOIDCTokenBody(
        token, jwk.getN(), jwk.getE());
  }

  private boolean validationIdToken(String idToken) throws JsonProcessingException {

    String[] idTokenArr = idToken.split("\\.");

    String header = idTokenArr[0];
    System.out.println("header = " + header);

    String payload = decodeBase64(idTokenArr[1]);
    System.out.println("payload = " + payload);

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

    String signature = idTokenArr[2];
    System.out.println("signature = " + signature);

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

  @Cacheable(cacheManager = "oidcCacheManager"
//      , cacheNames = "KakaoOICD"
      , key = "hello"
  )
  public JwkSet getPublicKeySet() {
    RestClient restClient = RestClient.builder()
        .baseUrl("https://kauth.kakao.com")
        .build();

    return restClient.get()
        .uri("/.well-known/jwks.json")
        .retrieve()
        .body(JwkSet.class);
  }

}
