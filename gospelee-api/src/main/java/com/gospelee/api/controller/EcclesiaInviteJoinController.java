package com.gospelee.api.controller;

import com.gospelee.api.dto.ecclesia.EcclesiaInviteInfoDTO;
import com.gospelee.api.service.EcclesiaService;
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
public class EcclesiaInviteJoinController {

  private final EcclesiaService ecclesiaService;

  private static final String DOWNLOAD_URL = "https://landing.podo.kr/#download";

  /**
   * 교회 초대 링크 웹 진입점
   * 모바일에서는 앱 딥링크(podo://ecclesia/join?code=...)를 자동으로 실행하고,
   * 앱이 없거나 PC인 경우 교회 소개 카드와 앱 열기/다운로드 안내를 제공합니다.
   */
  @GetMapping(value = "/ecclesia-invite/join", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> joinLanding(
      @RequestParam(value = "code", required = false) String code
  ) {
    String cleanCode = (code != null) ? code.trim() : "";
    EcclesiaInviteInfoDTO inviteInfo = null;

    if (!cleanCode.isEmpty()) {
      try {
        inviteInfo = ecclesiaService.getInviteInfo(cleanCode);
      } catch (Exception e) {
        log.debug("교회 초대 정보 조회 실패 또는 유효하지 않은 초대 코드: {}", cleanCode);
      }
    }

    String deepLinkUrl = !cleanCode.isEmpty()
        ? "podo://ecclesia/join?code=" + URLEncoder.encode(cleanCode, StandardCharsets.UTF_8)
        : "podo://ecclesia/join";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("text/html;charset=UTF-8"));
    return new ResponseEntity<>(buildHtml(cleanCode, inviteInfo, deepLinkUrl), headers,
        HttpStatus.OK);
  }

  private String buildHtml(String code, EcclesiaInviteInfoDTO inviteInfo, String deepLinkUrl) {
    String escapedDeepLink = HtmlUtils.htmlEscape(deepLinkUrl);

    String title;
    String subInfoHtml;
    if (inviteInfo != null) {
      title = HtmlUtils.htmlEscape(inviteInfo.getName()) + "에 초대합니다";
      StringBuilder subInfo = new StringBuilder();
      if (inviteInfo.getSeniorPastorName() != null && !inviteInfo.getSeniorPastorName().isBlank()) {
        subInfo.append("<span class=\"sub-badge\">담임목사 ")
            .append(HtmlUtils.htmlEscape(inviteInfo.getSeniorPastorName()))
            .append("</span>");
      }
      if (inviteInfo.getChurchAddress() != null && !inviteInfo.getChurchAddress().isBlank()) {
        subInfo.append("<span class=\"sub-badge\">")
            .append(HtmlUtils.htmlEscape(inviteInfo.getChurchAddress()))
            .append("</span>");
      }
      subInfo.append("<span class=\"sub-badge count\">")
          .append(inviteInfo.getMemberCount())
          .append("명 함께하는 중</span>");
      subInfoHtml = "<div class=\"badge-group\">" + subInfo + "</div>";
    } else {
      title = "유효하지 않은 초대 링크입니다";
      subInfoHtml = "";
    }

    String desc = inviteInfo != null
        ? "포도 앱에서 가입하기만 누르면 됩니다.<br>공지사항, 사진, 묵상을 교회 식구들과 함께 나눠보세요."
        : "초대 링크가 만료되었거나 잘못되었습니다.<br>교회에 새 초대 링크를 요청해주세요.";

    // 앱이 아직 없는 경우를 위해 다운로드 안내 클릭 시 초대 코드를 클립보드에 복사
    String escapedCodeJs = code.replaceAll("[^A-Za-z0-9_-]", "");
    // 안드로이드는 intent URL(앱 미설치 시 설치 안내 페이지로 이동)로 앱을 연다
    String intentUrl = "intent://ecclesia/join?code="
        + URLEncoder.encode(code, StandardCharsets.UTF_8)
        + "#Intent;scheme=podo;package=org.podo;S.browser_fallback_url="
        + URLEncoder.encode(DOWNLOAD_URL, StandardCharsets.UTF_8) + ";end";
    String escapedIntentUrl = HtmlUtils.htmlEscape(intentUrl);

    String actionHtml = inviteInfo != null
        ? """
            <div class="btn-group">
              <a href="%s" id="openBtn" class="open-btn"><span>포도 앱에서 가입하기</span></a>
              <a href="%s" class="download-btn" onclick="copyCode()"><span>앱이 없으신가요? 설치 안내</span></a>
            </div>
            """.formatted(escapedDeepLink, DOWNLOAD_URL)
        : "";

    return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <title>교회 초대</title>
          <meta property="og:title" content="포도 교회 초대">
          <meta property="og:description" content="포도 앱에서 교회에 함께해요.">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
              min-height: 100vh;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              background: linear-gradient(135deg, #f7faf8 0%%, #edf4ef 100%%);
              padding: 24px;
              font-family: -apple-system, BlinkMacSystemFont, 'Pretendard', 'Segoe UI', Roboto, sans-serif;
              color: #1e293b;
            }
            .card {
              background: #ffffff;
              border-radius: 24px;
              box-shadow: 0 12px 36px rgba(49, 96, 73, 0.08);
              border: 1px solid #e2ebe5;
              width: 100%%;
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
              font-size: 14px;
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
              background: #f1f5f9;
              color: #475569;
              padding: 5px 10px;
              border-radius: 8px;
              font-size: 13px;
              font-weight: 600;
            }
            .sub-badge.count { background: #ecfdf5; color: #059669; }
            .title {
              font-size: 24px;
              font-weight: 800;
              margin-bottom: 12px;
              line-height: 1.35;
              letter-spacing: -0.5px;
            }
            .desc {
              font-size: 16px;
              color: #64748b;
              line-height: 1.6;
              margin-bottom: 24px;
            }
            .btn-group { display: flex; flex-direction: column; gap: 12px; }
            .open-btn {
              background: #316049;
              color: #ffffff;
              padding: 18px 20px;
              border-radius: 14px;
              font-size: 18px;
              font-weight: 700;
              text-decoration: none;
              display: flex;
              align-items: center;
              justify-content: center;
            }
            .open-btn:active { transform: scale(0.98); }
            .download-btn {
              background: #f1f5f9;
              color: #334155;
              padding: 15px 20px;
              border-radius: 14px;
              font-size: 15px;
              font-weight: 600;
              text-decoration: none;
              display: flex;
              align-items: center;
              justify-content: center;
            }
          </style>
          <script type="text/javascript">
            function copyCode() {
              try {
                if (navigator.clipboard && '%s') {
                  navigator.clipboard.writeText('podo-ecclesia-invite:%s');
                }
              } catch (e) {}
            }
            window.onload = function() {
              var userAgent = navigator.userAgent || navigator.vendor || window.opera;
              var isMobile = /iPhone|iPad|iPod|Android/i.test(userAgent);
              var isAndroid = /Android/i.test(userAgent);
              var appUrl = isAndroid ? '%s' : '%s';

              var openBtn = document.getElementById('openBtn');
              if (openBtn) { openBtn.href = appUrl; }
              copyCode();

              // 카카오톡 인앱 브라우저는 앱 실행(커스텀 스키마)이 막히므로 외부 브라우저로 다시 연다
              if (/KAKAOTALK/i.test(userAgent) && '%s') {
                window.location.href = 'kakaotalk://web/openExternal?url=' + encodeURIComponent(window.location.href);
                return;
              }

              if (isMobile && '%s') {
                setTimeout(function() {
                  window.location.href = appUrl;
                }, 500);
              }
            };
          </script>
        </head>
        <body>
          <div class="card">
            <div class="badge"><span>⛪</span> 교회 초대</div>
            %s
            <h1 class="title">%s</h1>
            <p class="desc">%s</p>
            %s
          </div>
        </body>
        </html>
        """.formatted(
            escapedCodeJs,
            escapedCodeJs,
            escapedIntentUrl,
            escapedDeepLink,
            escapedCodeJs,
            escapedCodeJs,
            subInfoHtml,
            title,
            desc,
            actionHtml
        );
  }
}
