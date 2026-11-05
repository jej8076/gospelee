package com.gospelee.api.service.prayer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gospelee.api.dto.prayer.PrayerDTOs;
import java.lang.reflect.RecordComponent;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** 익명성 회귀 방지: 어떤 응답에도 작성자·기도자의 계정 식별자를 담지 않는다 */
class PrayerAnonymityTest {

  private static final Set<String> FORBIDDEN = Set.of(
      "authorUid", "accountUid", "responderUid", "reporterUid", "drawerUid", "author",
      "changedBy");

  @Test
  void 어떤_응답_레코드에도_계정_식별자가_없다() {
    int inspected = 0;
    for (Class<?> type : PrayerDTOs.class.getDeclaredClasses()) {
      if (!type.isRecord()) {
        continue;
      }
      inspected++;
      for (RecordComponent component : type.getRecordComponents()) {
        assertTrue(!FORBIDDEN.contains(component.getName()),
            type.getSimpleName() + " 에 " + component.getName() + " 이(가) 있습니다");
      }
    }
    assertTrue(inspected > 0);
  }
}
