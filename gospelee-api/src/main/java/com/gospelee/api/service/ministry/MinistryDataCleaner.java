package com.gospelee.api.service.ministry;

import com.gospelee.api.entity.Announcement;
import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.ReactionTargetType;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.repository.jpa.file.FileDetailsRepository;
import com.gospelee.api.service.reaction.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 모임이 삭제될 때 모임에 딸린 콘텐츠를 지운다. 콘텐츠 종류가 늘어날 때(공지 → 갤러리 → 기도 쪽지)
 * 이곳에 삭제를 추가한다. 첨부 파일은 공지 삭제와 같이 삭제 표시만 하고 실제 파일 정리는 기존 방식을 따른다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinistryDataCleaner {

  private final AnnouncementRepository announcementRepository;
  private final FileDetailsRepository fileDetailsRepository;
  private final ReactionService reactionService;

  public void deleteAll(Long ministryId) {
    for (Announcement announcement : announcementRepository
        .findByOrganizationTypeAndOrganizationId(OrganizationType.MINISTRY.name(), ministryId)) {
      if (announcement.getFileUid() != null) {
        for (FileDetails detail : fileDetailsRepository.findAllByFileIdAndDelYn(
            announcement.getFileUid(), Yn.N.name())) {
          detail.markAsDeleted();
          fileDetailsRepository.save(detail);
        }
      }
      reactionService.removeAllByTarget(ReactionTargetType.ANNOUNCEMENT, announcement.getId());
      announcementRepository.delete(announcement);
    }
    log.info("모임 콘텐츠 삭제 - ministryId:{}", ministryId);
  }
}
