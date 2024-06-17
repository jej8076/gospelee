package com.gospelee.api.utils;

import java.security.SecureRandom;

public class RandomStringGenerator {

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom random = new SecureRandom();

  public static String makeQrLoginRandomCode() {
    int codeLength = 12;
    StringBuilder sb = new StringBuilder(codeLength);
    for (int i = 0; i < codeLength; i++) {
      sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
  }
}
