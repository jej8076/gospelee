package com.gospelee.api.service.ministry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Ministry;
import com.gospelee.api.entity.MinistryMember;
import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import com.gospelee.api.enums.MinistryStatus;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.repository.jpa.journal.ContentReportRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryMemberRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryRepository;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AdminMinistryServiceTest {

  private MinistryRepository ministryRepository;
  private MinistryMemberRepository memberRepository;
  private AccountRepository accountRepository;
  private AnnouncementRepository announcementRepository;
  private ContentReportRepository reportRepository;
  private AdminMinistryService service;
  private Ministry worship;
  private Ministry mission;

  @BeforeEach
  void setUp() {
    ministryRepository = mock(MinistryRepository.class);
    memberRepository = mock(MinistryMemberRepository.class);
    accountRepository = mock(AccountRepository.class);
    announcementRepository = mock(AnnouncementRepository.class);
    reportRepository = mock(ContentReportRepository.class);
    when(reportRepository.findByStatusOrderByUidDesc("PENDING")).thenReturn(List.of());
    service = new AdminMinistryService(ministryRepository, memberRepository, accountRepository,
        announcementRepository, reportRepository);

    worship = Ministry.builder().name("청년 워십팀").build();
    ReflectionTestUtils.setField(worship, "id", 1L);
    mission = Ministry.builder().name("선교단체").build();
    ReflectionTestUtils.setField(mission, "id", 2L);
    when(ministryRepository.findAll()).thenReturn(List.of(worship, mission));
    when(ministryRepository.findById(1L)).thenReturn(Optional.of(worship));
    when(ministryRepository.findById(99L)).thenReturn(Optional.empty());

    when(memberRepository.countActiveByMinistry()).thenReturn(
        List.<Object[]>of(new Object[]{1L, 5L}, new Object[]{2L, 2L}));
    when(memberRepository.findByRoleAndStatus(MinistryRole.OWNER, MinistryMemberStatus.ACTIVE))
        .thenReturn(List.of(
            MinistryMember.builder().ministryId(1L).accountUid(10L).role(MinistryRole.OWNER)
                .status(MinistryMemberStatus.ACTIVE).build(),
            MinistryMember.builder().ministryId(2L).accountUid(20L).role(MinistryRole.OWNER)
                .status(MinistryMemberStatus.ACTIVE).build()));
    when(accountRepository.findAllById(any())).thenReturn(List.of(
        Account.builder().uid(10L).name("김대표").leaveYn(Yn.N).build(),
        Account.builder().uid(20L).name("이대표").nickname("선교리더").leaveYn(Yn.N).build()));
  }

  @Test
  void 목록은_멤버_수와_대표_이름을_채운다() {
    List<AdminMinistryService.Row> rows = service.list(null);
    assertEquals(2, rows.size());
    AdminMinistryService.Row first = rows.stream().filter(r -> r.id() == 1L).findFirst().get();
    assertEquals(5, first.memberCount());
    assertEquals("김대표", first.ownerName());
    AdminMinistryService.Row second = rows.stream().filter(r -> r.id() == 2L).findFirst().get();
    assertEquals("선교리더", second.ownerName());
    assertEquals(104857600L, second.storageLimitBytes());
  }

  @Test
  void 키워드는_모임_이름과_대표_이름에서_찾는다() {
    assertEquals(1, service.list("워십").size());
    assertEquals(1, service.list("선교리더").size());
    assertEquals(0, service.list("없는이름").size());
  }

  @Test
  void 정지하고_해제할_수_있다() {
    assertEquals("SUSPENDED", service.changeStatus(1L, true, 1L).status());
    assertEquals(MinistryStatus.SUSPENDED, worship.getStatus());
    assertEquals("ACTIVE", service.changeStatus(1L, false, 1L).status());
  }

  @Test
  void 없는_모임은_정지할_수_없다() {
    assertThrows(NoSuchElementException.class, () -> service.changeStatus(99L, true, 1L));
  }
}
