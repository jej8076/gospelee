package com.gospelee.api.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gospelee.api.dto.ministry.MinistryDtos.InviteInfo;
import org.junit.jupiter.api.Test;

class MinistryInviteJoinControllerTest {

  @Test
  void 유효한_초대는_모임_이름과_인원_앱_열기_링크를_보여준다() {
    String html = MinistryInviteJoinController.buildHtml("abc234xyz789",
        new InviteInfo("청년 워십팀", "함께 찬양해요", 12));

    assertTrue(html.contains("청년 워십팀에 초대합니다"));
    assertTrue(html.contains("12명 함께하는 중"));
    assertTrue(html.contains("podo://ministry/join?code=abc234xyz789"));
    assertTrue(html.contains("intent://ministry/join?code=abc234xyz789"));
    assertTrue(html.contains("abc234xyz789</b>"));
  }

  @Test
  void 모임_이름과_소개의_HTML은_이스케이프한다() {
    String html = MinistryInviteJoinController.buildHtml("abc234xyz789",
        new InviteInfo("<script>alert(1)</script>", "<img src=x onerror=alert(1)>", 1));

    assertFalse(html.contains("<script>alert(1)"));
    assertFalse(html.contains("<img src=x"));
    assertTrue(html.contains("&lt;script&gt;"));
  }

  @Test
  void 유효하지_않은_초대는_안내만_보여주고_가입_버튼이_없다() {
    String html = MinistryInviteJoinController.buildHtml("bad", null);

    assertTrue(html.contains("유효하지 않은 초대 링크입니다"));
    assertFalse(html.contains("openBtn\" class"));
  }
}
