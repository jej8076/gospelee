package com.gospelee.api.service.reaction;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.ReactionTargetType;
import com.gospelee.api.exception.AnnouncementNotFoundException;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.repository.jpa.ministry.MinistryMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** 공지사항: 내 교회의 공지, 내가 활성 멤버인 모임의 공지에만 반응할 수 있다 */
@Component
@RequiredArgsConstructor
public class AnnouncementReactionTargetPolicy implements ReactionTargetPolicy {

  private final AnnouncementRepository announcementRepository;
  // MinistryService 는 반응 정리 쪽(MinistryDataCleaner)과 순환하므로 멤버 저장소만 쓴다
  private final MinistryMemberRepository ministryMemberRepository;

  @Override
  public ReactionTargetType targetType() {
    return ReactionTargetType.ANNOUNCEMENT;
  }

  @Override
  public void assertReactable(AccountAuthDTO account, Long targetId) {
    Announcement announcement = announcementRepository.findById(targetId)
        .orElseThrow(() -> new AnnouncementNotFoundException("not_found_announcement id:%s", targetId));
    if (OrganizationType.MINISTRY.name().equals(announcement.getOrganizationType())) {
      boolean activeMember = ministryMemberRepository
          .findByMinistryIdAndAccountUid(announcement.getOrganizationId(), account.getUid())
          .map(member -> member.isActive())
          .orElse(false);
      if (!activeMember) {
        throw new AccessDeniedException("모임 멤버만 반응할 수 있습니다.");
      }
      return;
    }
    boolean sameEcclesia = OrganizationType.ECCLESIA.name().equals(announcement.getOrganizationType())
        && account.getEcclesiaUid() != null
        && account.getEcclesiaUid().equals(announcement.getOrganizationId());
    if (!sameEcclesia) {
      throw new AccessDeniedException("다른 교회의 공지에는 반응할 수 없습니다.");
    }
  }
}
