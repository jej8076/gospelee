package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 묵상 공유. 공유 시점의 내용을 스냅샷으로 보관한다 (원본을 수정해도 다시 공유하기 전까지 바뀌지 않음)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "journal_share", indexes = {
    @Index(name = "uk_journal_share_token", columnList = "token", unique = true),
    @Index(name = "idx_journal_share_journal", columnList = "journalUid")
})
public class JournalShare extends EditInfomation {

  public static final String ACTIVE = "ACTIVE";
  public static final String REVOKED = "REVOKED";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column(nullable = false)
  private Long journalUid;

  @Column(nullable = false)
  private Long accountUid;

  @Column(nullable = false, length = 32)
  private String token;

  @Column(length = 500)
  private String reference;

  @Column(columnDefinition = "TEXT")
  private String verseText;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false, length = 10)
  private String status;

  @Builder
  public JournalShare(Long journalUid, Long accountUid, String token, String reference,
      String verseText, String content) {
    this.journalUid = journalUid;
    this.accountUid = accountUid;
    this.token = token;
    this.reference = reference;
    this.verseText = verseText;
    this.content = content;
    this.status = ACTIVE;
  }

  /** 다시 공유: 스냅샷을 갱신하고 공유를 되살린다 */
  public void refresh(String reference, String verseText, String content) {
    this.reference = reference;
    this.verseText = verseText;
    this.content = content;
    this.status = ACTIVE;
  }

  public void revoke() {
    this.status = REVOKED;
  }

  public boolean isActive() {
    return ACTIVE.equals(status);
  }
}
