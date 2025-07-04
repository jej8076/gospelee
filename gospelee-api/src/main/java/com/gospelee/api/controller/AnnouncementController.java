package com.gospelee.api.controller;

import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.service.AnnouncementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/announcement")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @PostMapping("/{announcementType}")
  public ResponseEntity<Object> getAnnouncementList(
      @PathVariable("announcementType") String announcementType) {
    List<AnnouncementResponseDTO> announcementList = announcementService.getAnnouncementList(
        announcementType);
    return new ResponseEntity<>(announcementList, HttpStatus.OK);
  }

  @PostMapping("/{announcementType}/{id}")
  public ResponseEntity<Object> getAnnouncement(
      @PathVariable("announcementType") String announcementType,
      @PathVariable("id") Long id
  ) {
    AnnouncementDTO announcement = announcementService.getAnnouncement(announcementType, id);
    return new ResponseEntity<>(announcement, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> insertAnnouncement(
      @RequestPart(value = "files", required = false) List<MultipartFile> files,
      @RequestPart("body") @Valid AnnouncementDTO announcementDTO
  ) {
    log.info("공지사항 등록 요청 - 파일 개수: {}, DTO: {}", 
        files != null ? files.size() : 0, announcementDTO);
    
    if (files != null) {
      for (int i = 0; i < files.size(); i++) {
        log.info("파일 {}: 이름={}, 크기={}", i, files.get(i).getOriginalFilename(), files.get(i).getSize());
      }
    }
    
    AnnouncementDTO announcement = announcementService.insertAnnouncement(files, announcementDTO);
    return new ResponseEntity<>(announcement, HttpStatus.OK);
  }
}
