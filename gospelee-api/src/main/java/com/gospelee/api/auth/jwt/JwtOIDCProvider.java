package com.gospelee.api.auth.jwt;

import static util.Base64Util.decodeBase64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.dto.jwt.JwkDTO;
import com.gospelee.api.dto.jwt.JwkSetDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.entity.Account;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.RedisCacheService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
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
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Slf4j
public class JwtOIDCProvider {

  private final RedisCacheService redisCacheService;
  private final AccountService accountService;
  @Value("${kakao.issuer}")
  private String KAKAO_ISS;
  @Value("${kakao.app-key}")
  private String KAKAO_SERVICE_APP_KEY;
  private final String BEARER = "Bearer ";

  public JwtOIDCProvider(RedisCacheService redisCacheService, AccountService accountService) {
    this.redisCacheService = redisCacheService;
    this.accountService = accountService;
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

  private CharSequence getUnsignedToken(String token) {
    String[] splitToken = token.split("\\.");
    if (splitToken.length != 3) {
      throw new IllegalArgumentException("잘못된 Id Token 입니다.");
    }
    return splitToken[0] + "." + splitToken[1] + "." + splitToken[2];
  }

  private boolean validationIdToken(String idToken) throws JsonProcessingException {

    if (ObjectUtils.isEmpty(idToken)) {
      return false;
    }

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
      log.error("토큰이 만료되었습니다. [token : " + idToken + "]");
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

  public Authentication getAuthentication(JwtPayload jwtPayload, String idToken) {
    Optional<Account> accountOptional = accountService.saveAndGetAccount(jwtPayload, idToken);

    return accountOptional.map(account -> {
      UserDetails userDetails = Account.builder()
          .uid(account.getUid())
          .email(account.getEmail())
          .name(account.getName())
          .role(account.getRole())
          .id_token(account.getId_token())
          .ecclesiaUid(account.getEcclesiaUid())
          .pushToken(account.getPushToken())
          .build();
      return new UsernamePasswordAuthenticationToken(userDetails, idToken,
          userDetails.getAuthorities());
    }).orElseThrow(() -> new RuntimeException("account 불러오기 혹은 저장 실패"));
  }

}
