package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Journal extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column
  private Long accountUid;

  @Column
  private String content;

  @OneToMany(mappedBy = "journal", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<JournalBible> JournalBibleList;

  @Builder
  public Journal(long uid, long accountUid, String content, List<JournalBible> journalBibleList) {
    this.uid = uid;
    this.accountUid = accountUid;
    this.content = content;
    this.JournalBibleList = journalBibleList;
  }

  public void changeJournalBibleList(List<JournalBible> journalBibleList) {
    this.JournalBibleList = journalBibleList;
  }
}
