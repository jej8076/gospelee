package com.gospelee.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalBible {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column
  private int book;

  @Column
  private int chapter;

  @Column
  private int verse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "journalUid")
  private Journal journal;

  @Builder
  public JournalBible(long uid, int book, int chapter, int verse, Journal journal) {
    this.uid = uid;
    this.book = book;
    this.chapter = chapter;
    this.verse = verse;
    this.journal = journal;
  }
}
