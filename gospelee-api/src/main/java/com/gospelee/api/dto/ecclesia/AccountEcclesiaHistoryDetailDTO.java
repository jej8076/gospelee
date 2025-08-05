package com.gospelee.api.dto.ecclesia;

import com.gospelee.api.enums.AccountEcclesiaHistoryStatusType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountEcclesiaHistoryDetailDTO {

  private Long id;
  private long accountUid;
  private long ecclesiaUid;
  private String name;
  private String email;
  private String phone;
  private AccountEcclesiaHistoryStatusType status;
  private LocalDateTime insertTime;

  @Builder
  public AccountEcclesiaHistoryDetailDTO(Long id, long accountUid, long ecclesiaUid, String name,
      String email, String phone, AccountEcclesiaHistoryStatusType status,
      LocalDateTime insertTime) {
    this.id = id;
    this.accountUid = accountUid;
    this.ecclesiaUid = ecclesiaUid;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.status = status;
    this.insertTime = insertTime;
  }
}