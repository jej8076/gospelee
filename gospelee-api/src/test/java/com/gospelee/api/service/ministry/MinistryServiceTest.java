package com.gospelee.api.service.ministry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.ministry.MinistryDtos.Detail;
import com.gospelee.api.dto.ministry.MinistryDtos.JoinResult;
import com.gospelee.api.dto.ministry.MinistryDtos.LeaveResult;
import com.gospelee.api.dto.ministry.MinistryDtos.SaveRequest;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ministry;
import com.gospelee.api.entity.MinistryMember;
import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryMemberRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

/** 저장소를 메모리 리스트로 흉내 내어 한도·승계·가입 규칙을 확인한다 */
class MinistryServiceTest {

  private final List<Ministry> ministries = new ArrayList<>();
  private final List<MinistryMember> members = new ArrayList<>();
  private final AtomicLong ids = new AtomicLong(100);

  private MinistryRepository ministryRepository;
  private MinistryMemberRepository memberRepository;
  private AccountRepository accountRepository;
  private MinistryPushNotifier pushNotifier;
  private MinistryDataCleaner dataCleaner;
  private MinistryService service;

  @BeforeEach
  void setUp() {
    ministryRepository = mock(MinistryRepository.class);
    memberRepository = mock(MinistryMemberRepository.class);
    accountRepository = mock(AccountRepository.class);
    pushNotifier = mock(MinistryPushNotifier.class);
    dataCleaner = mock(MinistryDataCleaner.class);
    service = new MinistryService(ministryRepository, memberRepository, accountRepository,
        pushNotifier, dataCleaner);

    when(ministryRepository.save(any(Ministry.class))).thenAnswer(i -> {
      Ministry m = i.getArgument(0);
      if (m.getId() == null) {
        ReflectionTestUtils.setField(m, "id", ids.incrementAndGet());
        ministries.add(m);
      }
      return m;
    });
    when(ministryRepository.findById(anyLong())).thenAnswer(
        i -> ministries.stream().filter(m -> m.getId().equals(i.getArgument(0))).findFirst());
    when(ministryRepository.findByIdForUpdate(anyLong())).thenAnswer(
        i -> ministries.stream().filter(m -> m.getId().equals(i.getArgument(0))).findFirst());
    when(ministryRepository.findByInviteCode(any())).thenAnswer(
        i -> ministries.stream().filter(m -> i.getArgument(0).equals(m.getInviteCode()))
            .findFirst());
    org.mockito.Mockito.doAnswer(i -> ministries.remove((Ministry) i.getArgument(0)))
        .when(ministryRepository).delete(any(Ministry.class));

    when(memberRepository.save(any(MinistryMember.class))).thenAnswer(i -> {
      MinistryMember m = i.getArgument(0);
      if (m.getId() == null) {
        ReflectionTestUtils.setField(m, "id", ids.incrementAndGet());
        members.add(m);
      }
      return m;
    });
    when(memberRepository.saveAndFlush(any(MinistryMember.class))).thenAnswer(i -> i.getArgument(0));
    when(memberRepository.findByMinistryIdAndAccountUid(anyLong(), anyLong())).thenAnswer(
        i -> members.stream().filter(m -> m.getMinistryId().equals(i.getArgument(0))
            && m.getAccountUid().equals(i.getArgument(1))).findFirst());
    when(memberRepository.findByAccountUid(anyLong())).thenAnswer(
        i -> members.stream().filter(m -> m.getAccountUid().equals(i.getArgument(0))).toList());
    when(memberRepository.findById(anyLong())).thenAnswer(
        i -> members.stream().filter(m -> m.getId().equals(i.getArgument(0))).findFirst());
    when(memberRepository.findByMinistryIdAndStatus(anyLong(), any())).thenAnswer(
        i -> members.stream().filter(m -> m.getMinistryId().equals(i.getArgument(0))
            && m.getStatus() == i.getArgument(1)).toList());
    when(memberRepository.countByMinistryIdAndStatus(anyLong(), any())).thenAnswer(
        i -> members.stream().filter(m -> m.getMinistryId().equals(i.getArgument(0))
            && m.getStatus() == i.getArgument(1)).count());
    when(memberRepository.countByAccountUidAndRole(anyLong(), any())).thenAnswer(
        i -> members.stream().filter(m -> m.getAccountUid().equals(i.getArgument(0))
            && m.getRole() == i.getArgument(1)).count());
    when(memberRepository.countByAccountUidAndStatusIn(anyLong(), anyCollection())).thenAnswer(
        i -> members.stream().filter(m -> m.getAccountUid().equals(i.getArgument(0))
            && ((Collection<?>) i.getArgument(1)).contains(m.getStatus())).count());
    org.mockito.Mockito.doAnswer(i -> members.remove((MinistryMember) i.getArgument(0)))
        .when(memberRepository).delete(any(MinistryMember.class));
    org.mockito.Mockito.doAnswer(i -> members.removeIf(
            m -> m.getMinistryId().equals(i.getArgument(0))))
        .when(memberRepository).deleteByMinistryId(anyLong());

    when(accountRepository.findByUidForUpdate(anyLong())).thenAnswer(i -> Optional.of(account(i.getArgument(0))));
    when(accountRepository.findById(anyLong())).thenAnswer(i -> Optional.of(account(i.getArgument(0))));
    when(accountRepository.findAllById(any())).thenAnswer(i -> {
      List<Account> result = new ArrayList<>();
      for (Object uid : (Iterable<?>) i.getArgument(0)) {
        result.add(account((Long) uid));
      }
      return result;
    });
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private Account account(long uid) {
    return Account.builder().uid(uid).name("user" + uid).leaveYn(Yn.N).build();
  }

  private void login(long uid) {
    AccountAuthDTO account = AccountAuthDTO.builder().uid(uid).name("user" + uid)
        .role(com.gospelee.api.enums.RoleType.LAYMAN).build();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities()));
  }

  private Ministry newMinistry(long ownerUid) {
    login(ownerUid);
    Detail detail = service.create(new SaveRequest("모임" + ids.get(), null));
    return ministries.stream().filter(m -> m.getId().equals(detail.id())).findFirst().get();
  }

  private MinistryMember addMember(Ministry ministry, long uid, MinistryRole role,
      LocalDateTime joinedAt) {
    return memberRepository.save(MinistryMember.builder().ministryId(ministry.getId())
        .accountUid(uid).role(role).status(MinistryMemberStatus.ACTIVE).joinedAt(joinedAt)
        .build());
  }

  private MinistryRole roleOf(Ministry ministry, long uid) {
    return members.stream().filter(m -> m.getMinistryId().equals(ministry.getId())
        && m.getAccountUid() == uid).map(MinistryMember::getRole).findFirst().orElse(null);
  }

  // ---------------------------------------------------------------- 한도

  @Test
  void 대표인_모임은_세_개까지만_만들_수_있다() {
    newMinistry(1);
    newMinistry(1);
    newMinistry(1);
    assertThrows(IllegalArgumentException.class,
        () -> service.create(new SaveRequest("네번째", null)));
  }

  @Test
  void 가입한_모임은_승인_대기를_포함해_열_개까지다() {
    for (int i = 0; i < 9; i++) {
      Ministry m = newMinistry(10 + i);
      addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now());
    }
    Ministry target = newMinistry(50);
    target.changeInviteAutoApprove(false);
    login(2);
    JoinResult result = service.join(target.getInviteCode());
    assertEquals("PENDING", result.status());

    Ministry another = newMinistry(51);
    login(2);
    assertThrows(IllegalArgumentException.class, () -> service.join(another.getInviteCode()));
  }

  // ---------------------------------------------------------------- 가입

  @Test
  void 자동_승인이면_바로_가입하고_아니면_대기하며_대표와_운영진에게_알린다() {
    Ministry auto = newMinistry(1);
    auto.changeInviteAutoApprove(true);
    login(2);
    assertEquals("JOINED", service.join(auto.getInviteCode()).status());
    assertEquals("ALREADY_JOINED", service.join(auto.getInviteCode()).status());

    Ministry manual = newMinistry(1);
    login(3);
    assertEquals("PENDING", service.join(manual.getInviteCode()).status());
    assertEquals("PENDING", service.join(manual.getInviteCode()).status());
    verify(pushNotifier).notifyJoinRequested(any(Account.class), any(Ministry.class), any());
  }

  @Test
  void 내보낸_사람은_자동_승인_모임이어도_승인_대기를_거친다() {
    Ministry m = newMinistry(1);
    m.changeInviteAutoApprove(true);
    MinistryMember kicked = addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now());
    login(1);
    service.removeMember(m.getId(), 2L);
    assertEquals(MinistryMemberStatus.REMOVED, kicked.getStatus());

    login(2);
    assertEquals("PENDING", service.join(m.getInviteCode()).status());
    assertEquals(MinistryMemberStatus.PENDING, kicked.getStatus());
  }

  @Test
  void 정지된_모임은_가입할_수_없고_수정도_막힌다() {
    Ministry m = newMinistry(1);
    m.changeStatus(com.gospelee.api.enums.MinistryStatus.SUSPENDED);
    login(2);
    assertThrows(IllegalStateException.class, () -> service.join(m.getInviteCode()));
    login(1);
    assertThrows(IllegalStateException.class,
        () -> service.update(m.getId(), new SaveRequest("새이름", null)));
  }

  @Test
  void 코드를_재발급하면_이전_코드는_쓸_수_없다() {
    Ministry m = newMinistry(1);
    String old = m.getInviteCode();
    login(1);
    service.regenerateInvite(m.getId());
    login(2);
    assertThrows(IllegalArgumentException.class, () -> service.join(old));
  }

  // ---------------------------------------------------------------- 권한

  @Test
  void 일반_멤버는_운영_기능을_쓸_수_없다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now());
    login(2);
    assertThrows(AccessDeniedException.class, () -> service.getInvite(m.getId()));
    assertThrows(AccessDeniedException.class, () -> service.getRequests(m.getId()));
    assertThrows(AccessDeniedException.class, () -> service.delete(m.getId()));
    assertThrows(AccessDeniedException.class,
        () -> service.changeRole(m.getId(), 1L, MinistryRole.MEMBER));
    assertNull(service.get(m.getId()).inviteCode());
  }

  @Test
  void 멤버가_아니면_모임이_있는지도_알_수_없다() {
    Ministry m = newMinistry(1);
    login(9);
    assertThrows(java.util.NoSuchElementException.class, () -> service.get(m.getId()));
    assertThrows(java.util.NoSuchElementException.class, () -> service.getMembers(m.getId()));
  }

  @Test
  void 운영진은_일반_멤버만_내보낼_수_있다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MANAGER, LocalDateTime.now());
    addMember(m, 3, MinistryRole.MANAGER, LocalDateTime.now());
    addMember(m, 4, MinistryRole.MEMBER, LocalDateTime.now());
    login(2);
    assertThrows(AccessDeniedException.class, () -> service.removeMember(m.getId(), 3L));
    assertThrows(IllegalArgumentException.class, () -> service.removeMember(m.getId(), 1L));
    service.removeMember(m.getId(), 4L);
    assertFalse(members.stream().anyMatch(x -> x.getAccountUid() == 4 && x.isActive()));
  }

  @Test
  void 대표_양도는_상대의_대표_한도를_확인하고_기존_대표는_운영진이_된다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now());
    newMinistry(3);
    for (int i = 0; i < 2; i++) {
      newMinistry(3);
    }
    addMember(m, 3, MinistryRole.MEMBER, LocalDateTime.now());
    login(1);
    assertThrows(IllegalArgumentException.class,
        () -> service.changeRole(m.getId(), 3L, MinistryRole.OWNER));

    service.changeRole(m.getId(), 2L, MinistryRole.OWNER);
    assertEquals(MinistryRole.OWNER, roleOf(m, 2));
    assertEquals(MinistryRole.MANAGER, roleOf(m, 1));
  }

  // ---------------------------------------------------------------- 승계

  @Test
  void 양도_대상을_지정하면_그_사람이_대표가_된다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now().minusDays(5));
    addMember(m, 3, MinistryRole.MEMBER, LocalDateTime.now());
    login(1);
    LeaveResult result = service.leave(m.getId(), 3L);
    assertEquals("TRANSFERRED", result.result());
    assertEquals(MinistryRole.OWNER, roleOf(m, 3));
    assertNull(roleOf(m, 1));
  }

  @Test
  void 지정이_없으면_운영진_중_가입일이_가장_이른_사람이_이어받는다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now().minusDays(30));
    addMember(m, 3, MinistryRole.MANAGER, LocalDateTime.now().minusDays(2));
    addMember(m, 4, MinistryRole.MANAGER, LocalDateTime.now().minusDays(9));
    login(1);
    service.leave(m.getId(), null);
    assertEquals(MinistryRole.OWNER, roleOf(m, 4));
  }

  @Test
  void 운영진이_없으면_가입일이_가장_이른_멤버가_이어받는다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now().minusDays(1));
    addMember(m, 3, MinistryRole.MEMBER, LocalDateTime.now().minusDays(7));
    login(1);
    service.leave(m.getId(), null);
    assertEquals(MinistryRole.OWNER, roleOf(m, 3));
  }

  @Test
  void 대표_한도에_걸린_후보는_건너뛴다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now().minusDays(9));
    addMember(m, 3, MinistryRole.MEMBER, LocalDateTime.now().minusDays(1));
    for (int i = 0; i < 3; i++) {
      newMinistry(2);
    }
    login(1);
    service.leave(m.getId(), null);
    assertEquals(MinistryRole.OWNER, roleOf(m, 3));
  }

  @Test
  void 지정한_대상이_대표_한도에_걸리면_거절한다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now());
    for (int i = 0; i < 3; i++) {
      newMinistry(2);
    }
    login(1);
    assertThrows(IllegalArgumentException.class, () -> service.leave(m.getId(), 2L));
    assertEquals(MinistryRole.OWNER, roleOf(m, 1));
  }

  @Test
  void 멤버가_없으면_모임과_데이터를_삭제한다() {
    Ministry m = newMinistry(1);
    members.add(MinistryMember.builder().ministryId(m.getId()).accountUid(7L)
        .role(MinistryRole.MEMBER).status(MinistryMemberStatus.PENDING).build());
    login(1);
    LeaveResult result = service.leave(m.getId(), null);
    assertEquals("DELETED", result.result());
    assertFalse(ministries.contains(m));
    assertTrue(members.stream().noneMatch(x -> x.getMinistryId().equals(m.getId())));
    verify(dataCleaner).deleteAll(m.getId());
  }

  @Test
  void 대표가_아닌_멤버의_탈퇴는_멤버십만_지운다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now());
    login(2);
    assertEquals("LEFT", service.leave(m.getId(), null).result());
    assertEquals(MinistryRole.OWNER, roleOf(m, 1));
    assertTrue(ministries.contains(m));
  }

  @Test
  void 계정_탈퇴는_대표인_모임마다_승계_또는_삭제를_적용한다() {
    Ministry withMember = newMinistry(1);
    addMember(withMember, 2, MinistryRole.MEMBER, LocalDateTime.now());
    Ministry alone = newMinistry(1);
    Ministry joined = newMinistry(5);
    addMember(joined, 1, MinistryRole.MEMBER, LocalDateTime.now());

    service.leaveAll(1L);

    assertEquals(MinistryRole.OWNER, roleOf(withMember, 2));
    assertFalse(ministries.contains(alone));
    assertNull(roleOf(joined, 1));
    assertEquals(MinistryRole.OWNER, roleOf(joined, 5));
  }

  @Test
  void 대표가_삭제하면_멤버가_남아도_삭제된다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now());
    login(1);
    service.delete(m.getId());
    assertFalse(ministries.contains(m));
  }

  // ---------------------------------------------------------------- 콘텐츠 권한

  @Test
  void 공지는_대표와_운영진만_쓸_수_있고_정지된_모임은_막힌다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MANAGER, LocalDateTime.now());
    addMember(m, 3, MinistryRole.MEMBER, LocalDateTime.now());

    service.requireWriterOf(m.getId(), 1L);
    service.requireWriterOf(m.getId(), 2L);
    assertThrows(AccessDeniedException.class, () -> service.requireWriterOf(m.getId(), 3L));
    assertThrows(java.util.NoSuchElementException.class,
        () -> service.requireWriterOf(m.getId(), 9L));

    m.changeStatus(com.gospelee.api.enums.MinistryStatus.SUSPENDED);
    assertThrows(IllegalStateException.class, () -> service.requireWriterOf(m.getId(), 1L));
    // 정지돼도 멤버는 읽을 수 있다
    service.requireMemberOf(m.getId(), 3L);
  }

  @Test
  void 푸시_대상은_활성_멤버만이고_대기자와_내보낸_사람은_빠진다() {
    Ministry m = newMinistry(1);
    addMember(m, 2, MinistryRole.MEMBER, LocalDateTime.now());
    members.add(MinistryMember.builder().ministryId(m.getId()).accountUid(3L)
        .role(MinistryRole.MEMBER).status(MinistryMemberStatus.PENDING).build());
    members.add(MinistryMember.builder().ministryId(m.getId()).accountUid(4L)
        .role(MinistryRole.MEMBER).status(MinistryMemberStatus.REMOVED).build());

    List<Long> uids = service.getActiveMemberAccounts(m.getId()).stream()
        .map(Account::getUid).sorted().toList();
    assertEquals(List.of(1L, 2L), uids);
  }
}
