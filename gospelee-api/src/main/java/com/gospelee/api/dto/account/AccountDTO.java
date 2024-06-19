package com.gospelee.api.dto.account;

import com.gospelee.api.annotation.validation.PhoneNumber;
import com.gospelee.api.entity.RoleType;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class AccountDTO {

  @NotEmpty(message = "이름 정도는 알려줄 수 있잖아?")
  private String name;

  //    @RRN
//    @NotEmpty(message = "주민번호도 필수임 ㅋ")
  private String rrn;

  @PhoneNumber
  @NotEmpty(message = "핸드폰 번호는 필수 값입니다.. (엄 근 진)")
  private String phone;

  private RoleType roleType;

  private AccountKakaoTokenDTO accountKakaoTokenDTO;

  @Getter
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class QrRequest {

    private String email;
  }

}
