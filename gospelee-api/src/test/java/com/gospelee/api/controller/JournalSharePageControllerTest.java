package com.gospelee.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.journalshare.JournalShareDTOs.PublicShare;
import com.gospelee.api.service.JournalShareService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class JournalSharePageControllerTest {

  private final JournalShareService service = mock(JournalShareService.class);
  private final JournalSharePageController controller = new JournalSharePageController(service);

  @Test
  void 공유된_묵상은_OG_태그와_본문을_내려주고_사용자_입력은_이스케이프한다() {
    when(service.publicView("tok")).thenReturn(new PublicShare("tok", "요한복음 3:16",
        "16 하나님이 세상을", "<script>alert(1)</script>\n은혜", "<b>포도</b>", 2));

    ResponseEntity<String> response = controller.page("tok");
    String body = response.getBody();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(body.contains("og:title"));
    assertTrue(body.contains("요한복음 3:16"));
    assertTrue(body.contains("podo://journal-share/tok"));
    assertTrue(body.contains("댓글 2개"));
    assertFalse(body.contains("<script>alert(1)</script>"));
    assertFalse(body.contains("<b>포도</b>"));
  }

  @Test
  void 없거나_취소된_공유는_404() {
    when(service.publicView("none")).thenReturn(null);

    ResponseEntity<String> response = controller.page("none");

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(response.getBody().contains("묵상을 찾을 수 없어요"));
  }
}
