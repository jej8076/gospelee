package com.gospelee.api.controller;

import com.gospelee.api.dto.applink.AppLinkDTO;
import com.gospelee.api.service.AppLinkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-link")
@RequiredArgsConstructor
public class AppLinkController {

  private final AppLinkService appLinkService;

  /**
   * 딥링크 테스트 endpoint
   *
   * @param request
   * @param userAgent
   * @return
   */
  @GetMapping("/**")
  public ResponseEntity<String> appLink(HttpServletRequest request,
      @RequestHeader(value = "User-Agent", defaultValue = "") String userAgent
  ) {

    AppLinkDTO appLink = appLinkService.makeAppUrl(request, userAgent);

    // HTML로 앱 호출 시도 후 fallback
    String html = appLinkService.makeRedirectHtml()
        .formatted(appLink.getFallbackUrl(), appLink.getAppUrl());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.TEXT_HTML);
    return new ResponseEntity<>(html, headers, HttpStatus.OK);
  }

}
