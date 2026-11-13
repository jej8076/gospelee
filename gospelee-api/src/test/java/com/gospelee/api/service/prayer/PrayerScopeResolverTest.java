package com.gospelee.api.service.prayer;

import static com.gospelee.api.service.prayer.PrayerTestFixtures.member;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Ministry;
import com.gospelee.api.entity.MinistryMember;
import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import com.gospelee.api.enums.MinistryStatus;
import com.gospelee.api.repository.jpa.ministry.MinistryMemberRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class PrayerScopeResolverTest {

  private static final Long MINISTRY = 3L;
  private static final Long UID = 11L;

  private MinistryRepository ministryRepository;
  private MinistryMemberRepository memberRepository;
  private PrayerScopeResolver resolver;

  @BeforeEach
  void setUp() {
    ministryRepository = mock(MinistryRepository.class);
    memberRepository = mock(MinistryMemberRepository.class);
    resolver = new PrayerScopeResolver(ministryRepository, memberRepository);
    when(ministryRepository.findById(MINISTRY)).thenReturn(Optional.of(
        Ministry.builder().name("포도워십").inviteCode("abc").build()));
  }

  private void memberOf(MinistryRole role, MinistryMemberStatus status) {
    when(memberRepository.findByMinistryIdAndAccountUid(MINISTRY, UID)).thenReturn(Optional.of(
        MinistryMember.builder().ministryId(MINISTRY).accountUid(UID).role(role).status(status)
            .build()));
  }

  @Test
  void 모임_id가_없으면_계정_그대로_교회_범위() {
    AccountAuthDTO account = member(UID);
    assertSame(account, resolver.member(account, null));
    assertSame(account, resolver.manager(account, null));
  }

  @Test
  void 활성_멤버는_모임_키가_담긴_계정을_받고_교회_id와_겹치지_않는다() {
    memberOf(MinistryRole.MEMBER, MinistryMemberStatus.ACTIVE);

    AccountAuthDTO scoped = resolver.member(member(UID), MINISTRY);

    assertEquals(-MINISTRY, scoped.getEcclesiaUid());
    assertEquals("APL", scoped.getEcclesiaStatus());
    assertEquals(UID, scoped.getUid());
    assertEquals(MINISTRY, PrayerScopeResolver.ministryIdOf(scoped.getEcclesiaUid()));
    assertEquals(null, PrayerScopeResolver.ministryIdOf(7L));
  }

  @Test
  void 가입_대기와_내보낸_멤버는_거절한다() {
    memberOf(MinistryRole.MEMBER, MinistryMemberStatus.PENDING);
    assertThrows(AccessDeniedException.class, () -> resolver.member(member(UID), MINISTRY));

    memberOf(MinistryRole.MEMBER, MinistryMemberStatus.REMOVED);
    assertThrows(AccessDeniedException.class, () -> resolver.member(member(UID), MINISTRY));
  }

  @Test
  void 멤버가_아니면_거절한다() {
    when(memberRepository.findByMinistryIdAndAccountUid(MINISTRY, UID))
        .thenReturn(Optional.empty());
    assertThrows(AccessDeniedException.class, () -> resolver.member(member(UID), MINISTRY));
  }

  @Test
  void 정지된_모임은_거절한다() {
    Ministry suspended = Ministry.builder().name("정지").inviteCode("x").build();
    suspended.changeStatus(MinistryStatus.SUSPENDED);
    when(ministryRepository.findById(MINISTRY)).thenReturn(Optional.of(suspended));
    memberOf(MinistryRole.OWNER, MinistryMemberStatus.ACTIVE);

    assertThrows(AccessDeniedException.class, () -> resolver.member(member(UID), MINISTRY));
  }

  @Test
  void 관리자용은_대표와_운영진만_통과한다() {
    memberOf(MinistryRole.MEMBER, MinistryMemberStatus.ACTIVE);
    assertThrows(AccessDeniedException.class, () -> resolver.manager(member(UID), MINISTRY));

    memberOf(MinistryRole.MANAGER, MinistryMemberStatus.ACTIVE);
    assertEquals(-MINISTRY, resolver.manager(member(UID), MINISTRY).getEcclesiaUid());

    memberOf(MinistryRole.OWNER, MinistryMemberStatus.ACTIVE);
    assertEquals(-MINISTRY, resolver.manager(member(UID), MINISTRY).getEcclesiaUid());
  }

  @Test
  void 모임_관리자_계정은_서비스의_관리자_확인도_통과한다() {
    memberOf(MinistryRole.OWNER, MinistryMemberStatus.ACTIVE);
    AccountAuthDTO scoped = resolver.manager(member(UID), MINISTRY);

    // PrayerAccess 는 교회 직분으로 관리자를 판단한다. 모임 권한은 위에서 확인했다
    PrayerAccess.requireManager(scoped);
  }
}
