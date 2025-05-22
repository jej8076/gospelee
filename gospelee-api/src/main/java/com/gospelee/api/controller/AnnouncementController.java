package com.gospelee.api.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.journal.JournalDTO;
import com.gospelee.api.service.AnnouncementService;
import com.gospelee.api.service.FirebaseService;
import com.gospelee.api.service.JournalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/announcement")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  @GetMapping
  public ResponseEntity<Object> getAnnouncementList() {
    List<AnnouncementDTO> getJournalByAccountUid = announcementService.getAnnouncementList();
    return new ResponseEntity<>(getJournalByAccountUid, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> insertAnnouncement(
      @RequestBody @Valid AnnouncementDTO announcementDTO) {
    AnnouncementDTO announcement = announcementService.insertAnnouncement(announcementDTO);
    return new ResponseEntity<>(announcement, HttpStatus.OK);
  }

}
