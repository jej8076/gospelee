package com.gospelee.api.dto.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.entity.Account;
import com.gospelee.api.enums.RoleType;
import org.junit.jupiter.api.Test;

class AccountSummaryDTOTest {

  @Test
  void 민감정보는_응답에_포함되지_않는다() throws Exception {
    Account account = Account.builder()
        .uid(1L).name("홍길동").email("a@b.c").role(RoleType.PASTOR).ecclesiaUid(10L)
        .rrn("900101-1234567").idToken("secret-id-token").pushToken("secret-push-token")
        .build();

    String json = new ObjectMapper().writeValueAsString(AccountSummaryDTO.from(account));

    assertThat(json).contains("홍길동", "a@b.c", "PASTOR");
    assertThat(json).doesNotContain("rrn", "idToken", "pushToken", "secret", "900101");
  }
}
