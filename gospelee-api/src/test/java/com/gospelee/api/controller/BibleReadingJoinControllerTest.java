package com.gospelee.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.biblereading.BibleReadingGoalInviteInfoDTO;
import com.gospelee.api.service.BibleReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class BibleReadingJoinControllerTest {

  private BibleReadingService bibleReadingService;
  private BibleReadingJoinController controller;

  @BeforeEach
  void setUp() {
    bibleReadingService = mock(BibleReadingService.class);
    controller = new BibleReadingJoinController(bibleReadingService);
  }

  @Test
  void joinLanding_withoutCode_shouldReturnHtml() {
    ResponseEntity<String> response = controller.joinLanding(null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("말씀의 동행에 초대합니다"));
    assertTrue(response.getBody().contains("linear-gradient(135deg, #f7faf8 0%, #edf4ef 100%)"));
    assertTrue(response.getBody().contains("width: 100%;"));
  }

  @Test
  void joinLanding_withValidCode_shouldReturnHtmlWithInviteInfo() {
    BibleReadingGoalInviteInfoDTO inviteInfo = BibleReadingGoalInviteInfoDTO.builder()
        .title("2026 신약 일독")
        .rangeTypeLabel("신약")
        .orderTypeLabel("연대순")
        .participantCount(5)
        .build();

    when(bibleReadingService.getInviteInfo("ABCD123")).thenReturn(inviteInfo);

    ResponseEntity<String> response = controller.joinLanding("ABCD123");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("2026 신약 일독"));
    assertTrue(response.getBody().contains("ABCD123"));
    assertTrue(response.getBody().contains("5명 참여 중"));
    assertTrue(response.getBody().contains("podo://bible-reading/join?code=ABCD123"));
  }
}
