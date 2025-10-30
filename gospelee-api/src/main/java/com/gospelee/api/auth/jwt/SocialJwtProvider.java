package com.gospelee.api.auth.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.account.TokenDTO;
import com.gospelee.api.dto.jwt.JwtPayload;
import com.gospelee.api.enums.SocialLoginPlatform;
import com.gospelee.api.service.AccountService;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class SocialJwtProvider {

  private final AccountService accountService;

  protected SocialJwtProvider(AccountService accountService) {
    this.accountService = accountService;
  }

  public abstract SocialLoginPlatform getSupportedPlatform();

  public abstract JwtPayload getOIDCPayload(String token, String nonceCacheKey)
      throws JsonProcessingException;

  public Authentication getAuthentication(JwtPayload jwtPayload, TokenDTO tokenDTO) {
    Optional<AccountAuthDTO> accountAuthDTO = accountService.saveAndGetAccount(jwtPayload,
        tokenDTO);

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
          .build();
      return new UsernamePasswordAuthenticationToken(userDetails, tokenDTO.getIdToken(),
          userDetails.getAuthorities());
    }).orElseThrow(() -> new RuntimeException("account 불러오기 혹은 저장 실패"));
  }

  protected CharSequence getUnsignedToken(String token) {
    String[] splitToken = token.split("\\.");
    if (splitToken.length != 3) {
      throw new IllegalArgumentException("잘못된 Id Token 입니다.");
    }
    return splitToken[0] + "." + splitToken[1] + "." + splitToken[2];
  }

  protected PublicKey getRSAPublicKey(String modulus, String exponent)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
    byte[] decodeE = Base64.getUrlDecoder().decode(exponent);
    BigInteger n = new BigInteger(1, decodeN);
    BigInteger e = new BigInteger(1, decodeE);

    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
    return keyFactory.generatePublic(keySpec);
  }

}
