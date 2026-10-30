package com.gospelee.api.dto.pushgroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 푸시 그룹 만들기/수정 요청. 수정 시 멤버는 보낸 목록으로 통째로 바뀐다 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushGroupRequestDTO {

  private Long id;
  @NotBlank
  @Size(max = 30)
  private String name;
  @Size(max = 100)
  private String description;
  private List<Long> memberAccountUids;

  @Builder
  public PushGroupRequestDTO(Long id, String name, String description,
      List<Long> memberAccountUids) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.memberAccountUids = memberAccountUids;
  }
}
