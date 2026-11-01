package com.gospelee.api.service.bulletin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gospelee.api.enums.PushNotificationDataType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BulletinPushMessageTest {

  @Test
  void 알림을_누르면_주보_탭으로_연다() {
    assertEquals("/church?tab=bulletin",
        BulletinPushMessage.routeData().get(PushNotificationDataType.ROUTE.lower()));
  }

  @Test
  void 제목이_없으면_날짜로_문구를_만든다() {
    assertEquals("11월 1일 주보가 올라왔어요.", BulletinPushMessage.message(" ", LocalDate.of(2026, 11, 1)));
  }
}
