package com.gospelee.api.dto.gallery;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class GalleryFolderRequestDTO {

  private Long id;
  private Long parentId;

  @NotBlank(message = "폴더명을 입력해주세요.")
  private String name;

  private List<String> targetRoles;
  private Integer sortOrder;
}
