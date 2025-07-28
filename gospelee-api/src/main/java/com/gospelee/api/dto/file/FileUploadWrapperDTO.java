package com.gospelee.api.dto.file;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.enums.CategoryType;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUploadWrapperDTO {

  private CategoryType categoryType;
  private List<MultipartFile> files;
  private AccountAuthDTO accountAuth;
  private Long parentId;

  @Builder
  public FileUploadWrapperDTO(CategoryType categoryType, List<MultipartFile> files,
      AccountAuthDTO accountAuth, Long parentId) {
    this.categoryType = categoryType;
    this.files = files;
    this.accountAuth = accountAuth;
    this.parentId = parentId;
  }

  public void changeAccountAuth(AccountAuthDTO accountAuth) {
    this.accountAuth = accountAuth;
  }
}
