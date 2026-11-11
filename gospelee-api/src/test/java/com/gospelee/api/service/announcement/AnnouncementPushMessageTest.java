package com.gospelee.api.service.announcement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AnnouncementPushMessageTest {

  @Test
  void subjectIsUsedAsMessage() {
    assertEquals("이번 주 주일예배 안내", AnnouncementPushMessage.message("이번 주 주일예배 안내"));
  }

  @Test
  void routeData_pointsToAnnouncementDetail() {
    assertEquals("/ecclesia/announcement/42",
        AnnouncementPushMessage.routeData(42L).get("route"));
  }

  @Test
  void blankSubject_fallsBackToDefault() {
    assertEquals(AnnouncementPushMessage.DEFAULT_MESSAGE, AnnouncementPushMessage.message(null));
    assertEquals(AnnouncementPushMessage.DEFAULT_MESSAGE, AnnouncementPushMessage.message("  "));
  }

  @Test
  void newlinesAreCollapsed_andLongSubjectIsTruncated() {
    assertEquals("a b", AnnouncementPushMessage.message("a\n  b"));

    String message = AnnouncementPushMessage.message("가".repeat(100));
    assertEquals(63, message.length());
    assertTrue(message.endsWith("..."));
  }

  @Test
  void ministryTitle_putsMinistryNameInBrackets() {
    assertEquals("[청년 워십팀] 공지사항", AnnouncementPushMessage.ministryTitle("청년 워십팀"));
  }

  @Test
  void ministryTitle_truncatesLongNames_andFallsBackWhenBlank() {
    assertEquals("[" + "가".repeat(20) + "...] 공지사항",
        AnnouncementPushMessage.ministryTitle("가".repeat(25)));
    assertEquals("모임 공지사항", AnnouncementPushMessage.ministryTitle("  "));
  }

  @Test
  void ministryRouteData_carriesMinistryId() {
    assertEquals("/ecclesia/announcement/7?ministryId=3",
        AnnouncementPushMessage.ministryRouteData(7L, 3L).get("route"));
  }
}
