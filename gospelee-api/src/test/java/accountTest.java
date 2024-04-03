import static org.junit.jupiter.api.Assertions.assertTrue;
import static util.Base64Util.decodeBase64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

public class accountTest {

  private String KAKAO_ISS = "https://kauth.kakao.com";

  private String KAKAO_SERVICE_APP_KEY = "bc9ac0fd4cd17a858c971f6d4aede305";

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

  @Test
  void validationIdTokenTest() throws JsonProcessingException {
    String idToken = "eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJiYzlhYzBmZDRjZDE3YTg1OGM5NzFmNmQ0YWVkZTMwNSIsInN1YiI6IjMyNTI5MjcyMTIiLCJhdXRoX3RpbWUiOjE3MTIxMDE1MDksImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwibmlja25hbWUiOiLsoJXsnZjsp4QiLCJleHAiOjE3MTIxNDQ3MDksImlhdCI6MTcxMjEwMTUwOX0.K05eXKglWBSmrAl0oAFG8K6AchSZT4xyfCi2chtpd3QsdRLmwXVmGAhXZ-EiaoVYNhiyVdsrgFOB2zrOeAxHXQ8-jXigrlulja3CkKuVy43645moG033YO0Zpk0rRCDIcmTciwDoPVT39ERKw1rD6oN08YuDCMdUlxoD5yb4abXYkj_RsMtqeyp9Bmx6KA7Dbe3fFk1rSlzu6DvWUAeEkg94TwXMj8lgRLEmLP3G2-da_1apN-H9w8yMqG9GFg06QlSL_KmOujOdUOaMEYIf61miSUDQQA6bp0rTSSYG4rpxJZd2As5XhUocnEB7k3Stqkk5bFdJkE9HVgS8nuiIpg"; // Base64로 인코딩된 가정된 토큰 값
//    validationIdToken(idToken);
    assertTrue(validationIdToken(idToken));
  }

}
