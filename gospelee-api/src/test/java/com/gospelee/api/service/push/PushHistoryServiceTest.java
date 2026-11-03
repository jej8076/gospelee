package com.gospelee.api.service.push;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.pushhistory.PushHistoryDTO;
import com.gospelee.api.dto.pushhistory.PushHistoryDetailDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PushNotification;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationRepository;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.access.AccessDeniedException;

class PushHistoryServiceTest {

  private PushNotificationRepository pushRepository;
  private PushNotificationReceiversRepository receiversRepository;
  private AccountService accountService;
  private PushHistoryService service;
  private MockedStatic<AuthenticatedUserUtils> auth;

  @BeforeEach
  void setUp() {
    pushRepository = mock(PushNotificationRepository.class);
    receiversRepository = mock(PushNotificationReceiversRepository.class);
    accountService = mock(AccountService.class);
    service = new PushHistoryService(pushRepository, receiversRepository, accountService);
    auth = mockStatic(AuthenticatedUserUtils.class);
    loginAs(RoleType.PASTOR);
    when(accountService.getAccountByEcclesiaUid(10L)).thenReturn(Optional.of(List.of(
        Account.builder().uid(1L).name("목사").build(),
        Account.builder().uid(2L).name("성도2").build())));
  }

  @AfterEach
  void tearDown() {
    auth.close();
  }

  private void loginAs(RoleType role) {
    auth.when(AuthenticatedUserUtils::getAuthenticatedUserOrElseThrow).thenReturn(
        AccountAuthDTO.builder().uid(1L).role(role).ecclesiaUid(10L).build());
  }

  private PushNotification push(long id, long sender) {
    return PushNotification.builder().id(id).sendAccountUid(sender).title("t").message("m")
        .category("DIRECT").build();
  }

  private PushNotificationReceivers receiver(long accountUid, String status) {
    return PushNotificationReceivers.builder().pushNotificationId(5L)
        .receiveAccountUid(accountUid).status(status).build();
  }

  @Test
  void list_countsDeliveredReceivedConfirmedAndFailed() {
    when(pushRepository.findRecentByEcclesia(eq(10L), any())).thenReturn(List.of(push(5L, 1L)));
    when(receiversRepository.countByStatus(List.of(5L))).thenReturn(List.of(
        new Object[]{5L, "READY", 4L}, new Object[]{5L, "RECEIVED", 3L},
        new Object[]{5L, "CONFIRMED", 2L}, new Object[]{5L, "FAILED", 1L}));

    PushHistoryDTO dto = service.getList().get(0);

    assertEquals("목사", dto.getSenderName());
    assertEquals(10, dto.getTargetCount());
    assertEquals(9, dto.getDeliveredCount());
    assertEquals(1, dto.getFailedCount());
    assertEquals(5, dto.getReceivedCount());
    assertEquals(2, dto.getConfirmedCount());
  }

  @Test
  void detail_listsReceiversWithNames() {
    when(pushRepository.findById(5L)).thenReturn(Optional.of(push(5L, 1L)));
    when(receiversRepository.findByPushNotificationId(5L)).thenReturn(List.of(
        receiver(2L, "CONFIRMED"), receiver(99L, "READY")));

    PushHistoryDetailDTO detail = service.getDetail(5L);

    assertEquals("성도2", detail.getReceivers().get(0).getName());
    assertEquals("CONFIRMED", detail.getReceivers().get(0).getStatus());
    assertEquals("(알 수 없음)", detail.getReceivers().get(1).getName());
    assertEquals(1, detail.getPush().getConfirmedCount());
  }

  @Test
  void detail_ofAnotherChurchsPush_isNotFound() {
    when(pushRepository.findById(5L)).thenReturn(Optional.of(push(5L, 777L)));

    assertThrows(IllegalArgumentException.class, () -> service.getDetail(5L));
  }

  @Test
  void ordinaryMember_cannotView() {
    loginAs(RoleType.LAYMAN);

    assertThrows(AccessDeniedException.class, () -> service.getList());
    assertThrows(AccessDeniedException.class, () -> service.getDetail(5L));
  }
}
