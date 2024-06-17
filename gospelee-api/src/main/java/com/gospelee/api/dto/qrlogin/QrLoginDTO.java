package com.gospelee.api.dto.qrlogin;

import com.gospelee.api.entity.QrLogin;
import com.gospelee.api.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QrLoginDTO {

  public class Request {

    private String email;
    private String code;

    @Builder
    public Request(String email, String code) {
      this.email = email;
      this.code = code;
    }

    public static QrLogin toEntity(String email, String code) {
      return QrLogin.builder()
          .email(email)
          .code(code)
          // + expire time 1분 이후로 설정해야함
          .expireTime(TimeUtils.now())
          .build();
    }
  }
}
