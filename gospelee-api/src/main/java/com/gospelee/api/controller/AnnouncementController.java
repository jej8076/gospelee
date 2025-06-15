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
      @RequestPart(value = "file", required = false) MultipartFile file,
      @RequestPart("body") @Valid AnnouncementDTO announcementDTO
  ) {
    AnnouncementDTO announcement = announcementService.insertAnnouncement(file, announcementDTO);
    return new ResponseEntity<>(announcement, HttpStatus.OK);

  }

}
