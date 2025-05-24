package com.gospelee.api.dto.file;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.enums.FileCategoryType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUploadWrapperDTO {

  private FileCategoryType fileCategoryType;
  private MultipartFile file;
  private AccountAuthDTO accountAuth;
  private Long parentId;

  @Builder
  public FileUploadWrapperDTO(FileCategoryType fileCategoryType, MultipartFile file,
      AccountAuthDTO accountAuth, Long parentId) {
    this.fileCategoryType = fileCategoryType;
    this.file = file;
    this.accountAuth = accountAuth;
    this.parentId = parentId;
  }

  public void changeAccountAuth(AccountAuthDTO accountAuth) {
    this.accountAuth = accountAuth;
  }
}
