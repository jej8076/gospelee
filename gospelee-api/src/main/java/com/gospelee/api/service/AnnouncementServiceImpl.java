package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.repository.AnnouncementRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

  private final AnnouncementRepository announcementRepository;


  @Override
  public List<AnnouncementDTO> getAnnouncementList() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    // TODO 임시코드임
    return Collections.singletonList(AnnouncementDTO.builder().build());
  }

  @Override
  public AnnouncementDTO insertAnnouncement(AnnouncementDTO announcementDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    // TODO Announcement에 ecclesiaId 컬럼 필요
    // TODO insert 시 noti 발송 여부가 true이면 send noti도 진행해야하며 entity에 push noti 테이블 id값도 add 해놓아야 함
    return AnnouncementDTO.fromEntity(announcementRepository.save(announcementDTO.toEntity()));
  }
}


