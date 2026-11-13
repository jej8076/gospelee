package com.gospelee.api.service.reaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.entity.MinistryMember;
import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryMemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class AnnouncementReactionTargetPolicyTest {

  private static final Long MINISTRY = 3L;
  private static final Long ANNOUNCEMENT = 50L;

  private MinistryMemberRepository memberRepository;
  private AnnouncementReactionTargetPolicy policy;

  @BeforeEach
  void setUp() {
    AnnouncementRepository announcementRepository = mock(AnnouncementRepository.class);
    memberRepository = mock(MinistryMemberRepository.class);
    policy = new AnnouncementReactionTargetPolicy(announcementRepository, memberRepository);
    when(announcementRepository.findById(ANNOUNCEMENT)).thenReturn(Optional.of(
        Announcement.builder().id(ANNOUNCEMENT)
            .organizationType(OrganizationType.MINISTRY.name()).organizationId(MINISTRY)
            .subject("모임 공지").build()));
  }

  private static AccountAuthDTO account(long uid) {
    // 교회가 없는 계정이어도 모임 공지에는 반응할 수 있어야 한다
    return AccountAuthDTO.builder().uid(uid).build();
  }

  private void memberWith(MinistryMemberStatus status) {
    when(memberRepository.findByMinistryIdAndAccountUid(MINISTRY, 11L)).thenReturn(Optional.of(
        MinistryMember.builder().ministryId(MINISTRY).accountUid(11L).role(MinistryRole.MEMBER)
            .status(status).build()));
  }

  @Test
  void 모임_활성_멤버는_모임_공지에_반응할_수_있다() {
    memberWith(MinistryMemberStatus.ACTIVE);
    assertDoesNotThrow(() -> policy.assertReactable(account(11L), ANNOUNCEMENT));
  }

  @Test
  void 가입_대기나_멤버가_아니면_반응할_수_없다() {
    memberWith(MinistryMemberStatus.PENDING);
    assertThrows(AccessDeniedException.class,
        () -> policy.assertReactable(account(11L), ANNOUNCEMENT));

    when(memberRepository.findByMinistryIdAndAccountUid(MINISTRY, 12L))
        .thenReturn(Optional.empty());
    assertThrows(AccessDeniedException.class,
        () -> policy.assertReactable(account(12L), ANNOUNCEMENT));
  }
}
