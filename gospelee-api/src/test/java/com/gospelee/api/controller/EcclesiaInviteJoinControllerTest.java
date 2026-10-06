package com.gospelee.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.ecclesia.EcclesiaInviteInfoDTO;
import com.gospelee.api.exception.EcclesiaException;
import com.gospelee.api.service.EcclesiaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class EcclesiaInviteJoinControllerTest {

  private EcclesiaService ecclesiaService;
  private EcclesiaInviteJoinController controller;

  @BeforeEach
  void setUp() {
    ecclesiaService = mock(EcclesiaService.class);
    controller = new EcclesiaInviteJoinController(ecclesiaService);
  }

  @Test
  void joinLanding_withoutCode_shouldReturnInvalidHtml() {
    ResponseEntity<String> response = controller.joinLanding(null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("유효하지 않은 초대 링크입니다"));
    assertFalse(response.getBody().contains("포도 앱에서 가입하기"));
  }

  @Test
  void joinLanding_withValidCode_shouldReturnChurchInfoAndDeepLink() {
    when(ecclesiaService.getInviteInfo("abc234")).thenReturn(EcclesiaInviteInfoDTO.builder()
        .name("포도교회")
        .seniorPastorName("홍길동")
        .memberCount(12)
        .build());

    ResponseEntity<String> response = controller.joinLanding("abc234");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("포도교회에 초대합니다"));
    assertTrue(response.getBody().contains("담임목사 홍길동"));
    assertTrue(response.getBody().contains("12명 함께하는 중"));
    assertTrue(response.getBody().contains("podo://ecclesia/join?code=abc234"));
  }

  @Test
  void joinLanding_withInvalidCode_shouldNotLeakAndShowInvalid() {
    when(ecclesiaService.getInviteInfo("bad")).thenThrow(new EcclesiaException("유효하지 않은 초대 링크입니다."));

    ResponseEntity<String> response = controller.joinLanding("bad");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("유효하지 않은 초대 링크입니다"));
  }

  @Test
  void joinLanding_shouldEscapeHtmlInChurchName() {
    when(ecclesiaService.getInviteInfo("abc234")).thenReturn(EcclesiaInviteInfoDTO.builder()
        .name("<script>alert(1)</script>")
        .memberCount(1)
        .build());

    ResponseEntity<String> response = controller.joinLanding("abc234");

    assertFalse(response.getBody().contains("<script>alert(1)</script>"));
    assertTrue(response.getBody().contains("&lt;script&gt;"));
  }
}
