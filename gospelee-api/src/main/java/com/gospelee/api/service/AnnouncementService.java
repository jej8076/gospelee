package com.gospelee.api.service;

import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AnnouncementService {

  List<AnnouncementResponseDTO> getAnnouncementList(HttpServletRequest request,
      String announcementType);

  AnnouncementDTO getAnnouncement(String announcementType, Long id);

  AnnouncementDTO insertAnnouncement(List<MultipartFile> files, AnnouncementDTO announcementDTO);

  AnnouncementDTO updateAnnouncement(List<MultipartFile> files, AnnouncementDTO announcementDTO);
}
