package com.gospelee.api.service.push;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.pushhistory.PushReceiptRequestDTO;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PushReceiptServiceTest {

  private PushNotificationReceiversRepository repository;
  private PushReceiptService service;
  private MockedStatic<AuthenticatedUserUtils> auth;

  @BeforeEach
  void setUp() {
    repository = mock(PushNotificationReceiversRepository.class);
    service = new PushReceiptService(repository);
    auth = mockStatic(AuthenticatedUserUtils.class);
    auth.when(AuthenticatedUserUtils::getAuthenticatedUserOrElseThrow).thenReturn(
        AccountAuthDTO.builder().uid(7L).role(RoleType.LAYMAN).ecclesiaUid(10L).build());
  }

  @AfterEach
  void tearDown() {
    auth.close();
  }

  private PushNotificationReceivers receiver(String status) {
    PushNotificationReceivers r = PushNotificationReceivers.builder().id(1L)
        .pushNotificationId(5L).receiveAccountUid(7L).status(status).build();
    when(repository.findByPushNotificationIdAndReceiveAccountUid(5L, 7L))
        .thenReturn(Optional.of(r));
    return r;
  }

  private PushReceiptRequestDTO report(String status) {
    return PushReceiptRequestDTO.builder().pushId(5L).status(status).build();
  }

  @Test
  void deliveredMovesForward_toReceived_thenConfirmed() {
    PushNotificationReceivers r = receiver("READY");

    assertTrue(service.report(report("RECEIVED")));
    assertEquals("RECEIVED", r.getStatus());
    assertTrue(service.report(report("CONFIRMED")));
    assertEquals("CONFIRMED", r.getStatus());
  }

  @Test
  void lateReceivedReport_doesNotMoveConfirmedBackward() {
    PushNotificationReceivers r = receiver("CONFIRMED");

    assertFalse(service.report(report("RECEIVED")));
    assertEquals("CONFIRMED", r.getStatus());
  }

  @Test
  void tappingProvesDelivery_evenWhenFcmReportedFailure() {
    PushNotificationReceivers r = receiver("FAILED");

    assertTrue(service.report(report("CONFIRMED")));
    assertEquals("CONFIRMED", r.getStatus());
  }

  @Test
  void someoneElsesPush_isIgnored() {
    when(repository.findByPushNotificationIdAndReceiveAccountUid(5L, 7L))
        .thenReturn(Optional.empty());

    assertFalse(service.report(report("CONFIRMED")));
  }

  @Test
  void onlyReceivedOrConfirmedCanBeReported() {
    assertThrows(IllegalArgumentException.class, () -> service.report(report("FAILED")));
    assertThrows(IllegalArgumentException.class, () -> service.report(report("nonsense")));
  }
}
