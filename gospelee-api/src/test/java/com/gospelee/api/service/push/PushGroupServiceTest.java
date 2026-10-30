package com.gospelee.api.service.push;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.pushgroup.PushGroupCandidateDTO;
import com.gospelee.api.dto.pushgroup.PushGroupRequestDTO;
import com.gospelee.api.dto.pushgroup.PushGroupResponseDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PushGroup;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupMemberRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupRepository;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.access.AccessDeniedException;

class PushGroupServiceTest {

  private PushGroupRepository groupRepository;
  private PushGroupMemberRepository memberRepository;
  private AccountService accountService;
  private PushGroupService service;
  private MockedStatic<AuthenticatedUserUtils> auth;

  @BeforeEach
  void setUp() {
    groupRepository = mock(PushGroupRepository.class);
    memberRepository = mock(PushGroupMemberRepository.class);
    accountService = mock(AccountService.class);
    service = new PushGroupService(groupRepository, memberRepository, accountService);
    auth = mockStatic(AuthenticatedUserUtils.class);
    loginAs(RoleType.PASTOR, 10L);
    when(groupRepository.save(any(PushGroup.class))).thenAnswer(inv -> {
      PushGroup g = inv.getArgument(0);
      return PushGroup.builder().id(7L).ecclesiaUid(g.getEcclesiaUid()).name(g.getName())
          .description(g.getDescription()).build();
    });
    // 교회 10 의 성도: 1, 2 는 정상, 3 은 탈퇴
    when(accountService.getAccountByEcclesiaUid(10L)).thenReturn(Optional.of(List.of(
        account(1L, "김하나", "tok"), account(2L, "이둘", null), leftAccount(3L))));
  }

  @AfterEach
  void tearDown() {
    auth.close();
  }

  private void loginAs(RoleType role, Long ecclesiaUid) {
    auth.when(AuthenticatedUserUtils::getAuthenticatedUserOrElseThrow).thenReturn(
        AccountAuthDTO.builder().uid(1L).role(role).ecclesiaUid(ecclesiaUid).build());
  }

  private Account account(long uid, String name, String token) {
    return Account.builder().uid(uid).name(name).pushToken(token).role(RoleType.LAYMAN)
        .leaveYn(Yn.N).build();
  }

  private Account leftAccount(long uid) {
    return Account.builder().uid(uid).name("탈퇴").leaveYn(Yn.Y).build();
  }

  private PushGroupRequestDTO request(Long id, String name, List<Long> members) {
    return PushGroupRequestDTO.builder().id(id).name(name).memberAccountUids(members).build();
  }

  @Test
  void layman_cannotManageGroups() {
    loginAs(RoleType.LAYMAN, 10L);

    assertThrows(AccessDeniedException.class, () -> service.create(request(null, "청년부", null)));
    assertThrows(AccessDeniedException.class, () -> service.getList());
    assertThrows(AccessDeniedException.class, () -> service.getCandidates());
    verify(groupRepository, never()).save(any());
  }

  @Test
  void create_savesGroupAndMembers_dedupingMemberIds() {
    PushGroupResponseDTO dto = service.create(request(null, "  청년부 ", List.of(1L, 2L, 1L)));

    assertEquals("청년부", dto.getName());
    assertEquals(2, dto.getMemberCount());
    verify(memberRepository).saveAll(any());
  }

  @Test
  void create_rejectsMemberOutsideChurchOrLeft() {
    assertThrows(IllegalArgumentException.class,
        () -> service.create(request(null, "청년부", List.of(1L, 999L))));
    assertThrows(IllegalArgumentException.class,
        () -> service.create(request(null, "청년부", List.of(3L))));
    verify(groupRepository, never()).save(any());
  }

  @Test
  void create_rejectsDuplicateNameBlankNameAndTooManyGroups() {
    when(groupRepository.existsByEcclesiaUidAndName(10L, "청년부")).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> service.create(request(null, "청년부", null)));
    assertThrows(IllegalArgumentException.class, () -> service.create(request(null, "   ", null)));

    when(groupRepository.countByEcclesiaUid(10L))
        .thenReturn((long) PushGroupService.MAX_GROUPS_PER_ECCLESIA);
    assertThrows(IllegalArgumentException.class, () -> service.create(request(null, "새그룹", null)));
  }

  @Test
  void update_replacesMembers_andOtherChurchGroupIsNotFound() {
    PushGroup group = PushGroup.builder().id(7L).ecclesiaUid(10L).name("청년부").build();
    when(groupRepository.findByIdAndEcclesiaUid(7L, 10L)).thenReturn(Optional.of(group));
    when(groupRepository.findByIdAndEcclesiaUid(8L, 10L)).thenReturn(Optional.empty());

    PushGroupResponseDTO dto = service.update(request(7L, "청년부2", List.of(2L)));

    assertEquals("청년부2", dto.getName());
    assertEquals(List.of(2L), dto.getMemberAccountUids());
    verify(memberRepository).deleteByGroupId(7L);
    assertThrows(NoSuchElementException.class, () -> service.update(request(8L, "x", null)));
  }

  @Test
  void delete_removesMembersThenGroup() {
    PushGroup group = PushGroup.builder().id(7L).ecclesiaUid(10L).name("청년부").build();
    when(groupRepository.findByIdAndEcclesiaUid(7L, 10L)).thenReturn(Optional.of(group));

    service.delete(7L);

    verify(memberRepository).deleteByGroupId(7L);
    verify(groupRepository).delete(group);
    when(groupRepository.findByIdAndEcclesiaUid(9L, 10L)).thenReturn(Optional.empty());
    assertThrows(NoSuchElementException.class, () -> service.delete(9L));
  }

  @Test
  void candidates_excludeLeftMembers_andExposeOnlySafeFields() {
    List<PushGroupCandidateDTO> list = service.getCandidates();

    assertEquals(List.of(1L, 2L), list.stream().map(PushGroupCandidateDTO::getUid).toList());
    assertEquals(true, list.get(0).isPushEnabled());
    assertFalse(list.get(1).isPushEnabled());
  }
}
