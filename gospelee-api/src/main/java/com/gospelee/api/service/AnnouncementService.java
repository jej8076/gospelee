package com.gospelee.api.service;

import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AnnouncementService {

  List<AnnouncementResponseDTO> getAnnouncementList(String announcementType);

  AnnouncementDTO getAnnouncement(String announcementType, Long id);

  AnnouncementDTO insertAnnouncement(MultipartFile file, AnnouncementDTO announcementDTO);
}
