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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "journal_share_comment", indexes = {
    @Index(name = "idx_journal_share_comment_share", columnList = "shareUid")
})
public class JournalShareComment extends EditInfomation {

  public static final String ACTIVE = "ACTIVE";
  public static final String DELETED = "DELETED";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column(nullable = false)
  private Long shareUid;

  @Column(nullable = false)
  private Long accountUid;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(nullable = false, length = 10)
  private String status;

  public JournalShareComment(Long shareUid, Long accountUid, String content) {
    this.shareUid = shareUid;
    this.accountUid = accountUid;
    this.content = content;
    this.status = ACTIVE;
  }

  public void delete() {
    this.status = DELETED;
  }

  public boolean isActive() {
    return ACTIVE.equals(status);
  }
}
