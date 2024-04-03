package util;

import java.util.Base64;

public class Base64Util {

  private static final Base64.Decoder DECODER = Base64.getDecoder();

  public static String decodeBase64(String encodedString) {
    return new String(DECODER.decode(encodedString));
  }

}

