package com.gospelee.api.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EcclesiaVerificationTest {

  private Ecclesia ecclesia(LocalDateTime insertTime, Long storageLimit) {
    Ecclesia ecclesia = Ecclesia.builder()
        .uid(1L).name("포도교회").status("APL").storageLimitBytes(storageLimit).build();
    ReflectionTestUtils.setField(ecclesia, "insertTime", insertTime);
    return ecclesia;
  }

  @Test
  void newChurch_isUnverified() {
    assertFalse(ecclesia(LocalDateTime.now(), null).isVerified());
  }

  @Test
  void deadline_is14DaysAfterRegistration() {
    LocalDateTime registered = LocalDateTime.of(2026, 10, 1, 9, 0);
    Ecclesia ecclesia = ecclesia(registered, null);

    assertEquals(registered.plusDays(14), ecclesia.getVerificationDeadline());
    assertFalse(ecclesia.isVerificationExpiredAt(registered.plusDays(14)));
    assertTrue(ecclesia.isVerificationExpiredAt(registered.plusDays(14).plusSeconds(1)));
  }

  @Test
  void verifiedChurch_neverExpires() {
    LocalDateTime registered = LocalDateTime.of(2026, 1, 1, 9, 0);
    Ecclesia ecclesia = ecclesia(registered, null);
    ecclesia.changeVerified(true);

    assertTrue(ecclesia.isVerified());
    assertFalse(ecclesia.isVerificationExpiredAt(registered.plusYears(1)));
  }

  @Test
  void noInsertTime_hasNoDeadlineAndNeverExpires() {
    Ecclesia ecclesia = ecclesia(null, null);

    assertNull(ecclesia.getVerificationDeadline());
    assertFalse(ecclesia.isVerificationExpiredAt(LocalDateTime.now().plusYears(5)));
  }

  @Test
  void unverified_memberLimitIs30() {
    Ecclesia ecclesia = ecclesia(LocalDateTime.now(), null);

    assertTrue(ecclesia.hasRoomForMember(29));
    assertFalse(ecclesia.hasRoomForMember(30));

    ecclesia.changeVerified(true);
    assertTrue(ecclesia.hasRoomForMember(1000));
  }

  @Test
  void unverified_storageLimitIs100MB_evenIfConfiguredHigher() {
    Ecclesia ecclesia = ecclesia(LocalDateTime.now(), 10L * 1024 * 1024 * 1024);

    assertEquals(100L * 1024 * 1024, ecclesia.getStorageLimitBytesOrDefault());
    assertTrue(ecclesia.hasEnoughStorage(100L * 1024 * 1024));
    assertFalse(ecclesia.hasEnoughStorage(100L * 1024 * 1024 + 1));
  }

  @Test
  void verified_usesConfiguredStorageLimit() {
    Ecclesia ecclesia = ecclesia(LocalDateTime.now(), 10L * 1024 * 1024 * 1024);
    ecclesia.changeVerified(true);

    assertEquals(10L * 1024 * 1024 * 1024, ecclesia.getStorageLimitBytesOrDefault());
  }

  @Test
  void unverified_smallerConfiguredLimitIsKept() {
    Ecclesia ecclesia = ecclesia(LocalDateTime.now(), 50L * 1024 * 1024);

    assertEquals(50L * 1024 * 1024, ecclesia.getStorageLimitBytesOrDefault());
  }

  @Test
  void json_exposesVerificationInfo_butHidesInviteCode() throws Exception {
    Ecclesia ecclesia = ecclesia(LocalDateTime.of(2026, 10, 1, 9, 0), null);
    ecclesia.changeInviteCode("abc234abc234");
    ecclesia.changeInviteAutoApprove(true);

    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    JsonNode json = mapper.readTree(mapper.writeValueAsString(ecclesia));

    assertFalse(json.get("verified").asBoolean());
    assertTrue(json.has("verificationExpired"));
    assertEquals("2026-10-15T09:00:00", json.get("verificationDeadline").asText());
    // 다른 사용자가 교회 정보를 조회해도 초대 코드가 새어 나가지 않아야 한다
    assertFalse(json.has("inviteCode"));
    assertFalse(json.has("inviteAutoApprove"));
  }
}
