package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 콘텐츠(공유된 묵상, 댓글) 신고 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "content_report", uniqueConstraints = @UniqueConstraint(
    name = "uk_content_report", columnNames = {"reporterUid", "targetType", "targetUid"}))
public class ContentReport extends EditInfomation {

  public static final String TARGET_SHARE = "SHARE";
  public static final String TARGET_COMMENT = "COMMENT";

  public static final String PENDING = "PENDING";
  /** 신고 대상 콘텐츠를 내림 */
  public static final String RESOLVED = "RESOLVED";
  /** 문제 없음으로 기각 */
  public static final String DISMISSED = "DISMISSED";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long uid;

  @Column(nullable = false)
  private Long reporterUid;

  @Column(nullable = false, length = 10)
  private String targetType;

  @Column(nullable = false)
  private Long targetUid;

  @Column(nullable = false, length = 30)
  private String reason;

  @Column(length = 300)
  private String detail;

  @Column(nullable = false, length = 10)
  private String status;

  public ContentReport(Long reporterUid, String targetType, Long targetUid, String reason,
      String detail) {
    this.reporterUid = reporterUid;
    this.targetType = targetType;
    this.targetUid = targetUid;
    this.reason = reason;
    this.detail = detail;
    this.status = PENDING;
  }

  public void close(String status) {
    this.status = status;
  }

  public boolean isPending() {
    return PENDING.equals(status);
  }
}
