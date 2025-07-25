package com.gospelee.api.dto.journal;

import com.gospelee.api.entity.JournalBible;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JournalBibleDTO {

  private Long uid;

  private Long journalUid;

  private int book;

  private int cate;

  private int chapter;

  private int verse;

  @Builder
  public JournalBibleDTO(Long uid, Long journalUid, int book, int cate, int chapter, int verse) {
    this.uid = uid;
    this.journalUid = journalUid;
    this.book = book;
    this.cate = cate;
    this.chapter = chapter;
    this.verse = verse;
  }

  public static JournalBibleDTO toDto(JournalBible entity) {
    return JournalBibleDTO.builder()
        .uid(entity.getUid())
        .book(entity.getBook())
        .chapter(entity.getChapter())
        .verse(entity.getVerse())
        .build();
  }

  public static List<JournalBibleDTO> toDtoList(List<JournalBible> entityList) {
    return entityList.stream()
        .map(JournalBibleDTO::toDto)
        .collect(Collectors.toList());
  }
}


