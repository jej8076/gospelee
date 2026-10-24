package com.gospelee.api.service.reaction;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.ReactionTargetType;
import com.gospelee.api.exception.AnnouncementNotFoundException;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** 교회 공지사항: 내 교회의 공지에만 반응할 수 있다 */
@Component
@RequiredArgsConstructor
public class AnnouncementReactionTargetPolicy implements ReactionTargetPolicy {

  private final AnnouncementRepository announcementRepository;

  @Override
  public ReactionTargetType targetType() {
    return ReactionTargetType.ANNOUNCEMENT;
  }

  @Override
  public void assertReactable(AccountAuthDTO account, Long targetId) {
    Announcement announcement = announcementRepository.findById(targetId)
        .orElseThrow(() -> new AnnouncementNotFoundException("not_found_announcement id:%s", targetId));
    boolean sameEcclesia = OrganizationType.ECCLESIA.name().equals(announcement.getOrganizationType())
        && account.getEcclesiaUid() != null
        && account.getEcclesiaUid().equals(announcement.getOrganizationId());
    if (!sameEcclesia) {
      throw new AccessDeniedException("다른 교회의 공지에는 반응할 수 없습니다.");
    }
  }
}
