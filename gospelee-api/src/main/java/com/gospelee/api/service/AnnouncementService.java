package com.gospelee.api.service;

import com.gospelee.api.dto.announcement.AnnouncementDTO;
import java.util.List;

public interface AnnouncementService {

  List<AnnouncementDTO> getAnnouncementList();

  AnnouncementDTO insertAnnouncement(AnnouncementDTO announcementDTO);
}
