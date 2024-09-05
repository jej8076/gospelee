package com.gospelee.api.dto.journal;

import com.gospelee.api.entity.Journal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JournalDTO {

  private Long uid;

  private Long accountUid;

  private String content;

  private List<JournalBibleDTO> journalBibleList;

  @Builder
  public JournalDTO(Long uid, Long accountUid, String content,
      List<JournalBibleDTO> journalBibleList) {
    this.uid = uid;
    this.accountUid = accountUid;
    this.content = content;
    this.journalBibleList = journalBibleList;
  }

  public static JournalDTO toDto(Journal entity) {
    return JournalDTO.builder()
        .uid(entity.getUid())
        .accountUid(entity.getAccountUid())
        .content(entity.getContent())
        .journalBibleList(JournalBibleDTO.toDtoList(entity.getJournalBibleList()))
        .build();
  }

  public static List<JournalDTO> toDtoList(List<Journal> entityList) {
    return entityList.stream()
        .map(JournalDTO::toDto)
        .collect(Collectors.toList());
  }

}


