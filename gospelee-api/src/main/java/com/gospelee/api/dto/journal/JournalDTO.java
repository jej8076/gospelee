package com.gospelee.api.dto.journal;

import com.gospelee.api.entity.Journal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import util.DateUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JournalDTO {

  private Long uid;

  private Long accountUid;

  private String content;

  private List<JournalBibleDTO> journalBibleList;

  private String insertTime;

  private String updateTime;

  @Builder
  public JournalDTO(Long uid, Long accountUid, String content,
      List<JournalBibleDTO> journalBibleList,
      String insertTime, String updateTime) {
    this.uid = uid;
    this.accountUid = accountUid;
    this.content = content;
    this.journalBibleList = journalBibleList;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
  }

  public static JournalDTO toDto(Journal entity) {
    return JournalDTO.builder()
        .uid(entity.getUid())
        .accountUid(entity.getAccountUid())
        .content(entity.getContent())
        .journalBibleList(JournalBibleDTO.toDtoList(entity.getJournalBibleList()))
        .insertTime(DateUtils.timeToStringSec(entity.getInsertTime()))
        .updateTime(DateUtils.timeToStringSec(entity.getUpdateTime()))
        .build();
  }

  public static List<JournalDTO> toDtoList(List<Journal> entityList) {
    return entityList.stream()
        .map(JournalDTO::toDto)
        .collect(Collectors.toList());
  }

}


