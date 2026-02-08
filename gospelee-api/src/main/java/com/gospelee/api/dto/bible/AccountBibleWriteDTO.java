package com.gospelee.api.dto.bible;

import static com.gospelee.api.utils.BibleUtils.getCateByBook;

import com.gospelee.api.entity.AccountBibleWrite;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBibleWriteDTO {

  // 1(창세기)~66(요한계시록)
  @NotNull
  private Integer book;

  // N장
  @NotNull
  private Integer chapter;

  @Builder
  public AccountBibleWriteDTO(Integer book, Integer chapter) {
    this.book = book;
    this.chapter = chapter;
  }

  public static AccountBibleWriteDTO fromEntity(AccountBibleWrite entity) {
    return AccountBibleWriteDTO.builder()
        .book(entity.getBook())
        .chapter(entity.getChapter())
        .build();
  }

  public AccountBibleWrite toEntity(Long accountUid) {
    return AccountBibleWrite.builder()
        .accountUid(accountUid)
        .cate(getCateByBook(this.book))
        .book(this.book)
        .chapter(this.chapter)
        .build();
  }

}
