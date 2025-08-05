package com.gospelee.api.dto.account;

import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.enums.AccountEcclesiaHistoryStatusType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountEcclesiaHistoryDTO {

  private Long id;
  private long accountUid;
  private long ecclesiaUid;
  private AccountEcclesiaHistoryStatusType status;
  private LocalDateTime insertTime;

  @Builder
  public AccountEcclesiaHistoryDTO(Long id, long accountUid, long ecclesiaUid,
      AccountEcclesiaHistoryStatusType status,
      LocalDateTime insertTime) {
    this.id = id;
    this.accountUid = accountUid;
    this.ecclesiaUid = ecclesiaUid;
    this.status = status;
    this.insertTime = insertTime;
  }

  public static AccountEcclesiaHistoryDTO fromEntity(AccountEcclesiaHistory entity) {
    return AccountEcclesiaHistoryDTO.builder()
        .id(entity.getId())
        .accountUid(entity.getAccountUid())
        .ecclesiaUid(entity.getEcclesiaUid())
        .status(entity.getStatus())
        .insertTime(entity.getInsertTime())
        .build();
  }
}