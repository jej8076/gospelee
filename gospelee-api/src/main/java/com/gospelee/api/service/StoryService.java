package com.gospelee.api.service;

import com.gospelee.api.dto.announcement.AnnouncementDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface StoryService {

  AnnouncementDTO insertStory(List<MultipartFile> files, AnnouncementDTO announcementDTO);
}
