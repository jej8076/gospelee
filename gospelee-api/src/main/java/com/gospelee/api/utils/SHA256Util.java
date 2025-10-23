package com.gospelee.api.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Util {

  public static String hash(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

      return bytesToHex(encodedHash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error initializing SHA-256 algorithm", e);
    }
  }

  /**
   * 패스워드 비교 함수
   *
   * @param rawPassword    입력된 원본 패스워드
   * @param hashedPassword 저장된 해시 패스워드
   * @return 패스워드 일치 여부
   */
  public static boolean verifyPassword(String rawPassword, String hashedPassword) {
    String hash = hash(rawPassword);
    return hash.equals(hashedPassword);
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
