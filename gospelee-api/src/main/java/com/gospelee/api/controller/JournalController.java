package com.gospelee.api.controller;

import com.gospelee.api.dto.journal.JournalDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.service.JournalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/journal")
public class JournalController {

  private final JournalService journalService;

  /**
   * 로그인한 계정의 묵상기록 목록을 가져온다
   *
   * @param account
   * @return
   */
  @PostMapping
  public ResponseEntity<Object> getJournalByAccountUid(@AuthenticationPrincipal Account account) {
    List<JournalDTO> getJournalByAccountUid = journalService.getJournalList(account.getUid());
    return new ResponseEntity<>(getJournalByAccountUid, HttpStatus.OK);
  }

  /**
   * 로그인한 계정의 묵상기록을 등록한다
   *
   * @param account
   * @return
   */
  @PutMapping
  public ResponseEntity<Object> insertJournal(
      @AuthenticationPrincipal Account account,
      @RequestBody JournalDTO journalDTO) {
    JournalDTO insertJournal = journalService.insertJournal(account, journalDTO);
    return new ResponseEntity<>(insertJournal, HttpStatus.OK);
  }

}
