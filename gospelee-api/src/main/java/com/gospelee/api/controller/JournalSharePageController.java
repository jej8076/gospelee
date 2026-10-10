package com.gospelee.api.controller;

import com.gospelee.api.dto.journalshare.JournalShareDTOs.PublicShare;
import com.gospelee.api.service.JournalShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/**
 * 공유된 묵상의 비회원용 웹 페이지. 읽기만 가능하고 댓글은 앱에서 로그인 후 작성한다.
 * 카카오톡 등 링크 미리보기를 위해 OG 태그를 내려준다.
 */
@RestController
@RequiredArgsConstructor
public class JournalSharePageController {

  private static final String DOWNLOAD_URL = "https://landing.podo.kr/#download";
  private static final int DESCRIPTION_MAX = 90;

  private final JournalShareService shareService;

  @GetMapping(value = "/journal-share/{token}", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> page(@PathVariable String token) {
    PublicShare share = shareService.publicView(token);
    MediaType html = MediaType.valueOf("text/html;charset=UTF-8");
    if (share == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(html).body(notFoundHtml());
    }
    return ResponseEntity.ok().contentType(html).body(buildHtml(share));
  }

  static String buildHtml(PublicShare share) {
    String nickname = HtmlUtils.htmlEscape(share.authorNickname());
    String reference = HtmlUtils.htmlEscape(nullToEmpty(share.reference()));
    String verseText = toHtmlLines(share.verseText());
    String content = toHtmlLines(share.content());
    String deepLink = HtmlUtils.htmlEscape("podo://journal-share/" + share.token());
    String ogTitle = nickname + "님의 묵상" + (reference.isEmpty() ? "" : " · " + reference);
    String ogDescription = HtmlUtils.htmlEscape(summary(share.content()));

    return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <meta name="robots" content="noindex,nofollow">
          <title>%s</title>
          <meta property="og:type" content="article">
          <meta property="og:title" content="%s">
          <meta property="og:description" content="%s">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { min-height: 100vh; background: #f7faf8; padding: 24px 16px;
              font-family: -apple-system, BlinkMacSystemFont, 'Pretendard', 'Segoe UI', Roboto, sans-serif;
              color: #1e293b; }
            .card { max-width: 480px; margin: 0 auto; background: #fff; border-radius: 20px;
              border: 1px solid #e2ebe5; padding: 28px 24px; box-shadow: 0 12px 36px rgba(49,96,73,.08); }
            .author { font-size: 13px; color: #316049; font-weight: 700; margin-bottom: 14px; }
            .ref { font-size: 18px; font-weight: 800; margin-bottom: 10px; }
            .verse { background: #f1f7f3; border-radius: 12px; padding: 14px; font-size: 14px;
              line-height: 1.7; color: #334155; margin-bottom: 18px; }
            .content { font-size: 16px; line-height: 1.75; margin-bottom: 20px; word-break: break-word; }
            .meta { font-size: 13px; color: #64748b; margin-bottom: 20px; }
            .btn { display: block; text-align: center; text-decoration: none; padding: 15px;
              border-radius: 14px; font-weight: 700; font-size: 15px; background: #316049; color: #fff; }
            .btn.sub { background: #f1f5f9; color: #334155; margin-top: 10px; font-weight: 600; }
            .hint { font-size: 12px; color: #94a3b8; text-align: center; margin-top: 14px; }
          </style>
        </head>
        <body>
          <div class="card">
            <div class="author">%s님이 나눈 묵상</div>
            <div class="ref">%s</div>
            <div class="verse">%s</div>
            <div class="content">%s</div>
            <div class="meta">댓글 %d개</div>
            <a class="btn" href="%s">포도 앱에서 열고 댓글 달기</a>
            <a class="btn sub" href="%s">앱 다운로드 안내</a>
            <p class="hint">댓글은 앱에 가입한 뒤 작성할 수 있어요.</p>
          </div>
        </body>
        </html>
        """.formatted(ogTitle, ogTitle, ogDescription, nickname, reference, verseText, content,
        share.commentCount(), deepLink, DOWNLOAD_URL);
  }

  private static String notFoundHtml() {
    return """
        <!DOCTYPE html>
        <html lang="ko"><head><meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="robots" content="noindex,nofollow">
        <title>묵상을 찾을 수 없어요</title></head>
        <body style="font-family:-apple-system,sans-serif;text-align:center;padding:80px 24px;color:#334155">
          <h2>묵상을 찾을 수 없어요</h2>
          <p style="margin-top:12px;color:#64748b">삭제되었거나 공유가 취소된 묵상입니다.</p>
        </body></html>
        """;
  }

  private static String toHtmlLines(String text) {
    return HtmlUtils.htmlEscape(nullToEmpty(text)).replace("\r\n", "\n").replace("\n", "<br>");
  }

  private static String summary(String content) {
    String flat = nullToEmpty(content).replaceAll("\\s+", " ").trim();
    return flat.length() <= DESCRIPTION_MAX ? flat : flat.substring(0, DESCRIPTION_MAX) + "…";
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
