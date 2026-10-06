package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EcclesiaPushNotifierTest {

  private FirebaseService firebaseService;
  private AccountRepository accountRepository;
  private EcclesiaPushNotifier notifier;

  @BeforeEach
  void setUp() {
    firebaseService = mock(FirebaseService.class);
    accountRepository = mock(AccountRepository.class);
    notifier = new EcclesiaPushNotifier(firebaseService, accountRepository);
  }

  private Account account(Long uid, RoleType role, String pushToken) {
    Account account = mock(Account.class);
    when(account.getUid()).thenReturn(uid);
    when(account.getRole()).thenReturn(role);
    when(account.getPushToken()).thenReturn(pushToken);
    return account;
  }

  private Ecclesia ecclesia(long uid, Long masterUid) {
    Ecclesia ecclesia = mock(Ecclesia.class);
    when(ecclesia.getUid()).thenReturn(uid);
    when(ecclesia.getName()).thenReturn("포도교회");
    when(ecclesia.getMasterAccountUid()).thenReturn(masterUid);
    return ecclesia;
  }

  @Test
  void notifyJoinRequested_sendsToManagersOnly_withApprovalRoute() {
    Account master = account(1L, RoleType.SENIOR_PASTOR, "master-token");
    Account pastor = account(2L, RoleType.PASTOR, "pastor-token");
    Account layman = account(3L, RoleType.LAYMAN, "layman-token");
    when(accountRepository.findByEcclesiaUid(10L))
        .thenReturn(Optional.of(List.of(master, pastor, layman)));
    when(accountRepository.findById(1L)).thenReturn(Optional.of(master));

    notifier.notifyJoinRequested(ecclesia(10L, 1L), "김성도");

    // 마스터는 중복 없이 한 번만, 일반 성도는 제외
    verify(firebaseService, times(1)).sendNotification(eq("master-token"), anyString(), anyString(), any());
    verify(firebaseService, times(1)).sendNotification(eq("pastor-token"), anyString(), anyString(), any());
    verify(firebaseService, never()).sendNotification(eq("layman-token"), anyString(), anyString(), any());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
    ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
    verify(firebaseService).sendNotification(eq("pastor-token"), anyString(), body.capture(), data.capture());
    assertTrue(body.getValue().contains("김성도님이 포도교회 가입을 요청했어요"));
    assertTrue(data.getValue().get("route").equals("/ecclesia/requests"));
  }

  @Test
  void notifyJoinRequested_masterWithoutPushToken_isSkipped() {
    Account master = account(1L, RoleType.SENIOR_PASTOR, null);
    when(accountRepository.findByEcclesiaUid(10L)).thenReturn(Optional.of(List.of(master)));
    when(accountRepository.findById(1L)).thenReturn(Optional.of(master));

    notifier.notifyJoinRequested(ecclesia(10L, 1L), null);

    verify(firebaseService, never()).sendNotification(anyString(), anyString(), anyString(), any());
  }

  @Test
  void notifyJoinRequested_pushFailure_doesNotThrow() {
    Account master = account(1L, RoleType.SENIOR_PASTOR, "master-token");
    when(accountRepository.findByEcclesiaUid(10L)).thenReturn(Optional.of(List.of(master)));
    when(accountRepository.findById(1L)).thenReturn(Optional.of(master));
    when(firebaseService.sendNotification(anyString(), anyString(), anyString(), any()))
        .thenThrow(new RuntimeException("fcm down"));

    notifier.notifyJoinRequested(ecclesia(10L, 1L), "김성도");
  }

  @Test
  void notifyJoinDecided_sendsApprovedOrRejectedMessageToChurchTab() {
    Account target = account(5L, RoleType.LAYMAN, "target-token");

    notifier.notifyJoinDecided(target, "포도교회", true);
    notifier.notifyJoinDecided(target, "포도교회", false);

    ArgumentCaptor<String> title = ArgumentCaptor.forClass(String.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
    verify(firebaseService, times(2))
        .sendNotification(eq("target-token"), title.capture(), anyString(), data.capture());
    assertTrue(title.getAllValues().contains("교회 가입 승인"));
    assertTrue(title.getAllValues().contains("교회 가입 반려"));
    assertTrue(data.getAllValues().stream().allMatch(d -> "/church".equals(d.get("route"))));
  }

  @Test
  void isManagerRole() {
    assertTrue(EcclesiaPushNotifier.isManagerRole(RoleType.SENIOR_PASTOR));
    assertTrue(EcclesiaPushNotifier.isManagerRole(RoleType.PASTOR));
    assertTrue(EcclesiaPushNotifier.isManagerRole(RoleType.ADMIN));
    assertFalse(EcclesiaPushNotifier.isManagerRole(RoleType.LAYMAN));
    assertFalse(EcclesiaPushNotifier.isManagerRole(null));
  }
}
