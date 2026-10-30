package com.gospelee.api.service.push;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PushGroup;
import com.gospelee.api.entity.PushGroupMember;
import com.gospelee.api.entity.PushNotification;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupMemberRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationRepository;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.FirebaseService;
import com.gospelee.api.service.push.PushDispatchService.PushRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PushDispatchServiceTest {

  private AccountService accountService;
  private PushGroupRepository groupRepository;
  private PushGroupMemberRepository memberRepository;
  private PushNotificationRepository notificationRepository;
  private PushNotificationReceiversRepository receiversRepository;
  private FirebaseService firebaseService;
  private PushDispatchService service;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    accountService = mock(AccountService.class);
    groupRepository = mock(PushGroupRepository.class);
    memberRepository = mock(PushGroupMemberRepository.class);
    notificationRepository = mock(PushNotificationRepository.class);
    receiversRepository = mock(PushNotificationReceiversRepository.class);
    firebaseService = mock(FirebaseService.class);
    service = new PushDispatchService(accountService, groupRepository, memberRepository,
        notificationRepository, receiversRepository, firebaseService);

    when(notificationRepository.save(any(PushNotification.class))).thenAnswer(inv -> {
      PushNotification n = inv.getArgument(0);
      return PushNotification.builder().id(99L).title(n.getTitle()).message(n.getMessage())
          .build();
    });
    when(receiversRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    when(firebaseService.trySendNotification(anyString(), anyString(), anyString(), anyMap()))
        .thenReturn(true);
    // 교회 10 의 성도: 1,2,3 은 토큰 있음, 4 는 토큰 없음
    when(accountService.getAccountByEcclesiaUid(10L)).thenReturn(Optional.of(List.of(
        account(1L, "tok1"), account(2L, "tok2"), account(3L, "tok3"), account(4L, null))));
  }

  private Account account(long uid, String token) {
    return Account.builder().uid(uid).name("n" + uid).pushToken(token).leaveYn(Yn.N).build();
  }

  private PushRequest request(List<Long> groupIds) {
    return PushRequest.builder().sendAccountUid(1L).ecclesiaUid(10L).groupIds(groupIds)
        .organization("ECCLESIA").category("ANNOUNCEMENT").title("t").message("m")
        .data(Map.of("route", "/x")).build();
  }

  private PushGroup group(long id) {
    return PushGroup.builder().id(id).ecclesiaUid(10L).name("g" + id).build();
  }

  private PushGroupMember member(long groupId, long accountUid) {
    return PushGroupMember.builder().groupId(groupId).accountUid(accountUid).build();
  }

  @SuppressWarnings("unchecked")
  private List<PushNotificationReceivers> savedReceivers() {
    ArgumentCaptor<List<PushNotificationReceivers>> captor = ArgumentCaptor.forClass(List.class);
    verify(receiversRepository).saveAll(captor.capture());
    return captor.getValue();
  }

  @Test
  void noGroups_sendsToEveryChurchMemberWithToken() {
    Long id = service.send(request(null));

    assertEquals(99L, id);
    verify(firebaseService).trySendNotification(eq("tok1"), anyString(), anyString(), anyMap());
    verify(firebaseService).trySendNotification(eq("tok2"), anyString(), anyString(), anyMap());
    verify(firebaseService).trySendNotification(eq("tok3"), anyString(), anyString(), anyMap());
    assertEquals(3, savedReceivers().size());
    verify(notificationRepository).updateTotalcountById(99L, 3);
  }

  @Test
  void groups_sendOnlyToUnionOfMembers_onceEach() {
    when(groupRepository.findByIdInAndEcclesiaUid(any(), eq(10L)))
        .thenReturn(List.of(group(1L), group(2L)));
    // 2번 성도는 두 그룹에 모두 속해 있다
    when(memberRepository.findByGroupIdIn(List.of(1L, 2L))).thenReturn(List.of(
        member(1L, 1L), member(1L, 2L), member(2L, 2L)));

    service.send(request(List.of(1L, 2L, 2L)));

    verify(firebaseService).trySendNotification(eq("tok1"), anyString(), anyString(), anyMap());
    verify(firebaseService).trySendNotification(eq("tok2"), anyString(), anyString(), anyMap());
    verify(firebaseService, never()).trySendNotification(eq("tok3"), anyString(), anyString(),
        anyMap());
    assertEquals(2, savedReceivers().size());
  }

  @Test
  void groupMemberFromAnotherChurchOrLeftChurch_isNotSent() {
    when(groupRepository.findByIdInAndEcclesiaUid(any(), eq(10L))).thenReturn(List.of(group(1L)));
    // 777 은 우리 교회 성도 목록에 없다 (교회를 옮겼거나 탈퇴)
    when(memberRepository.findByGroupIdIn(List.of(1L))).thenReturn(List.of(
        member(1L, 3L), member(1L, 777L)));

    service.send(request(List.of(1L)));

    verify(firebaseService).trySendNotification(eq("tok3"), anyString(), anyString(), anyMap());
    assertEquals(1, savedReceivers().size());
  }

  @Test
  void groupOfAnotherChurch_isRejected_beforeAnythingIsSaved() {
    // 요청한 2개 중 1개만 내 교회 그룹
    when(groupRepository.findByIdInAndEcclesiaUid(any(), eq(10L))).thenReturn(List.of(group(1L)));

    assertThrows(IllegalArgumentException.class, () -> service.send(request(List.of(1L, 55L))));

    verify(notificationRepository, never()).save(any());
    verify(firebaseService, never()).trySendNotification(anyString(), anyString(), anyString(),
        anyMap());
  }

  @Test
  void failedSend_isRecordedAsFailed_andOthersStillGoOut() {
    when(firebaseService.trySendNotification(eq("tok2"), anyString(), anyString(), anyMap()))
        .thenReturn(false);

    service.send(request(null));

    List<PushNotificationReceivers> receivers = savedReceivers();
    assertEquals(3, receivers.size());
    assertEquals("FAILED", receivers.stream()
        .filter(r -> r.getReceiveAccountUid() == 2L).findFirst().orElseThrow().getStatus());
    assertEquals(2, receivers.stream().filter(r -> "READY".equals(r.getStatus())).count());
  }

  @Test
  void noRecipients_createsNothing() {
    when(accountService.getAccountByEcclesiaUid(10L)).thenReturn(Optional.empty());

    assertNull(service.send(request(null)));

    verify(notificationRepository, never()).save(any());
  }

  @Test
  void validateGroupIds_dedupes_andTreatsEmptyAsAll() {
    when(groupRepository.findByIdInAndEcclesiaUid(any(), eq(10L)))
        .thenReturn(List.of(group(1L), group(2L)));

    assertEquals(List.of(1L, 2L), service.validateGroupIds(10L, List.of(1L, 2L, 1L)));
    assertEquals(List.of(), service.validateGroupIds(10L, null));
    assertEquals(List.of(), service.validateGroupIds(10L, List.of()));
  }
}
