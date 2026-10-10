package com.gospelee.api.dto.journalshare;

import java.util.List;

public final class JournalShareDTOs {

  private JournalShareDTOs() {
  }

  /** 공유 생성 결과 */
  public record ShareResult(String token, String url, String reference, String authorNickname) {

  }

  public record CommentItem(Long uid, Long accountUid, String nickname, String content,
                            boolean mine, String insertTime) {

  }

  /** 로그인한 사용자가 보는 공유 상세 */
  public record ShareView(String token, Long shareUid, String reference, String verseText, String content,
                          Long authorUid, String authorNickname, boolean mine, String sharedAt,
                          List<CommentItem> comments) {

  }

  /** 비회원 웹 페이지용 공유 내용 (댓글은 개수만) */
  public record PublicShare(String token, String reference, String verseText, String content,
                            String authorNickname, long commentCount) {

  }

  public record CommentRequest(String content) {

  }

  public record ReportRequest(String targetType, Long targetUid, String reason, String detail) {

  }

  public record BlockRequest(Long accountUid) {

  }

  public record BlockedUser(Long accountUid, String nickname) {

  }
}
