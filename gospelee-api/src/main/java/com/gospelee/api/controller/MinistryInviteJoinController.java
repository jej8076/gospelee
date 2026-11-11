package com.gospelee.api.controller;

import com.gospelee.api.dto.ministry.MinistryDtos.InviteInfo;
import com.gospelee.api.service.ministry.MinistryService;
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

/**
 * 모임 초대 링크 웹 진입점. 모바일에서는 앱 딥링크(podo://ministry/join?code=...)를 열고,
 * 앱이 없거나 PC면 모임 소개와 설치 안내를 보여 준다. 모임 이름·소개·인원만 노출하고 멤버 목록은 주지 않는다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MinistryInviteJoinController {

  private static final String DOWNLOAD_URL = "https://landing.podo.kr/#download";

  private final MinistryService ministryService;

  @GetMapping(value = "/ministry-invite/join", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> joinLanding(
      @RequestParam(value = "code", required = false) String code) {
    String cleanCode = code != null ? code.trim() : "";
    InviteInfo info = null;
    if (!cleanCode.isEmpty()) {
      try {
        info = ministryService.getInviteInfo(cleanCode);
      } catch (Exception e) {
        log.debug("모임 초대 정보 조회 실패 또는 유효하지 않은 초대 코드");
      }
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("text/html;charset=UTF-8"));
    return new ResponseEntity<>(buildHtml(cleanCode, info), headers, HttpStatus.OK);
  }

  static String buildHtml(String code, InviteInfo info) {
    String encoded = URLEncoder.encode(code, StandardCharsets.UTF_8);
    String deepLink = HtmlUtils.htmlEscape("podo://ministry/join?code=" + encoded);
    // 안드로이드는 intent URL(앱 미설치 시 설치 안내로 이동)로 앱을 연다
    String intentUrl = HtmlUtils.htmlEscape("intent://ministry/join?code=" + encoded
        + "#Intent;scheme=podo;package=org.podo;S.browser_fallback_url="
        + URLEncoder.encode(DOWNLOAD_URL, StandardCharsets.UTF_8) + ";end");

    String title;
    String desc;
    String extra = "";
    String actions = "";
    if (info != null) {
      title = HtmlUtils.htmlEscape(info.name()) + "에 초대합니다";
      desc = info.description() != null && !info.description().isBlank()
          ? HtmlUtils.htmlEscape(info.description()).replace("\n", "<br>")
          : "포도 앱에서 가입하기만 누르면 됩니다.";
      extra = "<div class=\"count\">" + info.memberCount() + "명 함께하는 중</div>";
      actions = """
          <div class="btn-group">
            <a href="%s" id="openBtn" class="open-btn">포도 앱에서 가입하기</a>
            <a href="%s" class="download-btn">앱이 없으신가요? 설치 안내</a>
            <p class="hint">설치한 뒤 앱의 모임 메뉴에서 초대 코드 <b>%s</b>를 입력해도 가입할 수 있어요.</p>
          </div>
          """.formatted(deepLink, DOWNLOAD_URL, HtmlUtils.htmlEscape(code));
    } else {
      title = "유효하지 않은 초대 링크입니다";
      desc = "초대 링크가 만료되었거나 잘못되었습니다.<br>모임 대표에게 새 초대 링크를 요청해주세요.";
    }

    return """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <title>모임 초대</title>
          <meta property="og:title" content="포도 모임 초대">
          <meta property="og:description" content="포도 앱에서 모임에 함께해요.">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
              min-height: 100vh; display: flex; align-items: center; justify-content: center;
              background: linear-gradient(135deg, #f7faf8 0%%, #edf4ef 100%%); padding: 24px;
              font-family: -apple-system, BlinkMacSystemFont, 'Pretendard', 'Segoe UI', Roboto, sans-serif;
              color: #1e293b;
            }
            .card {
              background: #fff; border-radius: 24px; border: 1px solid #e2ebe5; width: 100%%;
              max-width: 420px; padding: 36px 28px; text-align: center;
              box-shadow: 0 12px 36px rgba(49, 96, 73, 0.08);
            }
            .badge {
              display: inline-block; background: #edf6f0; color: #316049; padding: 6px 14px;
              border-radius: 20px; font-size: 14px; font-weight: 700; margin-bottom: 20px;
            }
            .title { font-size: 24px; font-weight: 800; margin-bottom: 12px; line-height: 1.35; }
            .desc { font-size: 16px; color: #64748b; line-height: 1.6; margin-bottom: 16px; }
            .count {
              display: inline-block; background: #ecfdf5; color: #059669; padding: 5px 10px;
              border-radius: 8px; font-size: 13px; font-weight: 600; margin-bottom: 24px;
            }
            .btn-group { display: flex; flex-direction: column; gap: 12px; }
            .open-btn {
              background: #316049; color: #fff; padding: 18px 20px; border-radius: 14px;
              font-size: 18px; font-weight: 700; text-decoration: none;
            }
            .download-btn {
              background: #f1f5f9; color: #334155; padding: 15px 20px; border-radius: 14px;
              font-size: 15px; font-weight: 600; text-decoration: none;
            }
            .hint { font-size: 13px; color: #94a3b8; line-height: 1.5; }
          </style>
          <script>
            window.onload = function() {
              var ua = navigator.userAgent || '';
              var isAndroid = /Android/i.test(ua);
              var isMobile = /iPhone|iPad|iPod|Android/i.test(ua);
              var appUrl = isAndroid ? '%s' : '%s';
              var openBtn = document.getElementById('openBtn');
              if (!openBtn) { return; }
              openBtn.href = appUrl;
              // 카카오톡 인앱 브라우저는 앱 실행이 막혀 외부 브라우저로 다시 연다
              if (/KAKAOTALK/i.test(ua)) {
                window.location.href = 'kakaotalk://web/openExternal?url=' + encodeURIComponent(window.location.href);
                return;
              }
              if (isMobile) { setTimeout(function() { window.location.href = appUrl; }, 500); }
            };
          </script>
        </head>
        <body>
          <div class="card">
            <div class="badge">👥 모임 초대</div>
            <h1 class="title">%s</h1>
            <p class="desc">%s</p>
            %s
            %s
          </div>
        </body>
        </html>
        """.formatted(intentUrl, deepLink, title, desc, extra, actions);
  }
}
