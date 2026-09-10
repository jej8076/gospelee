package com.gospelee.api.controller;

import com.gospelee.api.dto.biblereading.BibleReadingGoalInviteInfoDTO;
import com.gospelee.api.service.BibleReadingService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BibleReadingJoinController {

  private final BibleReadingService bibleReadingService;

  private static final String DOWNLOAD_URL = "https://landing.podo.kr/#download";

  /**
   * 통독 목표 초대 링크 웹 진입점
   * 모바일에서는 앱 딥링크(podo://bible-reading/join?code=...)를 자동으로 실행하고,
   * 웹 브라우저에서는 초대 정보 카드 및 앱 열기/다운로드 안내 UI를 제공합니다.
   */
  @GetMapping(value = {"/bible-reading/join", "/bible/reading/join"}, produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> joinLanding(
      @RequestParam(value = "code", required = false) String code
  ) {
    String cleanCode = (code != null) ? code.trim() : "";
    BibleReadingGoalInviteInfoDTO inviteInfo = null;

    if (!cleanCode.isEmpty()) {
      try {
        inviteInfo = bibleReadingService.getInviteInfo(cleanCode);
      } catch (Exception e) {
        log.debug("초대 정보 조회 실패 또는 유효하지 않은 초대 코드: {}", cleanCode);
      }
    }

    String deepLinkUrl = !cleanCode.isEmpty()
        ? "podo://bible-reading/join?code=" + URLEncoder.encode(cleanCode, StandardCharsets.UTF_8)
        : "podo://bible-reading";

    String html = buildHtml(cleanCode, inviteInfo, deepLinkUrl);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("text/html;charset=UTF-8"));
    return new ResponseEntity<>(html, headers, HttpStatus.OK);
  }

  private String buildHtml(String code, BibleReadingGoalInviteInfoDTO inviteInfo, String deepLinkUrl) {
    String escapedCode = HtmlUtils.htmlEscape(code);
    String escapedDeepLink = HtmlUtils.htmlEscape(deepLinkUrl);

    String title;
    String subInfoHtml;

    if (inviteInfo != null) {
      title = HtmlUtils.htmlEscape(inviteInfo.getTitle());
      StringBuilder subInfo = new StringBuilder();
      if (inviteInfo.getRangeTypeLabel() != null && !inviteInfo.getRangeTypeLabel().isBlank()) {
        subInfo.append("<span class=\"sub-badge\">")
            .append(HtmlUtils.htmlEscape(inviteInfo.getRangeTypeLabel()))
            .append("</span>");
      }
      if (inviteInfo.getOrderTypeLabel() != null && !inviteInfo.getOrderTypeLabel().isBlank()) {
        subInfo.append("<span class=\"sub-badge\">")
            .append(HtmlUtils.htmlEscape(inviteInfo.getOrderTypeLabel()))
            .append("</span>");
      }
      subInfo.append("<span class=\"sub-badge count\">")
          .append(inviteInfo.getParticipantCount())
          .append("명 참여 중</span>");
      subInfoHtml = "<div class=\"badge-group\">" + subInfo + "</div>";
    } else {
      title = "말씀의 동행에 초대합니다";
      subInfoHtml = "";
    }

    String codeBoxHtml = !escapedCode.isEmpty()
        ? "<div class=\"code-box\">초대 코드: <strong>" + escapedCode + "</strong></div>"
        : "";

    return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <title>포도 성경 통독 함께하기</title>
          <meta property="og:title" content="포도 성경 통독 함께하기">
          <meta property="og:description" content="지체와 함께 목표를 나누고 매일 말씀을 통독해보세요.">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
              min-height: 100vh;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              background: linear-gradient(135deg, #f7faf8 0%, #edf4ef 100%);
              padding: 24px;
              font-family: -apple-system, BlinkMacSystemFont, 'Pretendard', 'Segoe UI', Roboto, sans-serif;
              color: #1e293b;
            }
            .card {
              background: #ffffff;
              border-radius: 24px;
              box-shadow: 0 12px 36px rgba(49, 96, 73, 0.08);
              border: 1px solid #e2ebe5;
              width: 100%;
              max-width: 420px;
              padding: 36px 28px;
              text-align: center;
            }
            .badge {
              display: inline-flex;
              align-items: center;
              gap: 6px;
              background: #edf6f0;
              color: #316049;
              padding: 6px 14px;
              border-radius: 20px;
              font-size: 13px;
              font-weight: 700;
              margin-bottom: 20px;
            }
            .badge-group {
              display: flex;
              flex-wrap: wrap;
              gap: 6px;
              justify-content: center;
              margin-bottom: 16px;
            }
            .sub-badge {
              display: inline-flex;
              align-items: center;
              background: #f1f5f9;
              color: #475569;
              padding: 4px 10px;
              border-radius: 8px;
              font-size: 12px;
              font-weight: 600;
            }
            .sub-badge.count {
              background: #ecfdf5;
              color: #059669;
            }
            .title {
              font-size: 22px;
              font-weight: 800;
              color: #1e293b;
              margin-bottom: 12px;
              line-height: 1.35;
              letter-spacing: -0.5px;
            }
            .desc {
              font-size: 15px;
              color: #64748b;
              line-height: 1.6;
              margin-bottom: 24px;
            }
            .code-box {
              background: #f8fafc;
              border: 1px dashed #cbd5e1;
              border-radius: 12px;
              padding: 12px;
              font-size: 14px;
              color: #475569;
              margin-bottom: 24px;
            }
            .code-box strong {
              color: #316049;
              font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
              font-size: 16px;
              letter-spacing: 0.5px;
            }
            .btn-group {
              display: flex;
              flex-direction: column;
              gap: 12px;
            }
            .open-btn {
              background: #316049;
              color: #ffffff;
              border: none;
              padding: 16px 20px;
              border-radius: 14px;
              font-size: 16px;
              font-weight: 700;
              cursor: pointer;
              text-decoration: none;
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 8px;
              transition: background 0.2s, transform 0.1s;
            }
            .open-btn:hover { background: #274d3a; }
            .open-btn:active { transform: scale(0.98); }
            .download-btn {
              background: #f1f5f9;
              color: #334155;
              border: none;
              padding: 14px 20px;
              border-radius: 14px;
              font-size: 15px;
              font-weight: 600;
              cursor: pointer;
              text-decoration: none;
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 6px;
            }
            .download-btn:hover { background: #e2e8f0; }
          </style>
          <script type="text/javascript">
            window.onload = function() {
              var userAgent = navigator.userAgent || navigator.vendor || window.opera;
              var isMobile = /iPhone|iPad|iPod|Android/i.test(userAgent);
              if (isMobile) {
                setTimeout(function() {
                  window.location.href = '%s';
                }, 500);
              }
            };
          </script>
        </head>
        <body>
          <div class="card">
            <div class="badge">
              <span>📖</span> 함께하는 성경 통독
            </div>
            %s
            <h1 class="title">%s</h1>
            <p class="desc">
              지체와 함께 목표를 나누고 매일 말씀을 통독해보세요.<br>
              포도 앱에서 즉시 목표에 참여할 수 있습니다.
            </p>
            %s
            <div class="btn-group">
              <a href="%s" class="open-btn">
                <span>포도 앱에서 열기</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
              </a>
              <a href="%s" class="download-btn">
                <span>앱 다운로드 안내</span>
              </a>
            </div>
          </div>
        </body>
        </html>
        """.formatted(
            escapedDeepLink,
            subInfoHtml,
            title,
            codeBoxHtml,
            escapedDeepLink,
            DOWNLOAD_URL
        );
  }
}
