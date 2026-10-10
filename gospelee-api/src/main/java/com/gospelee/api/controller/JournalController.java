package com.gospelee.api.controller;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.journal.JournalDTO;
import com.gospelee.api.service.JournalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  public ResponseEntity<Object> getJournalByAccountUid(
      @AuthenticationPrincipal AccountAuthDTO account) {
    List<JournalDTO> getJournalByAccountUid = journalService.getJournalList(account.getUid());
    return new ResponseEntity<>(getJournalByAccountUid, HttpStatus.OK);
  }

  /**
   * 로그인한 계정의 묵상기록을 삭제한다. 공유 중이면 공유 링크도 닫힌다
   */
  @DeleteMapping("/{journalUid}")
  public ResponseEntity<Object> deleteJournal(@AuthenticationPrincipal AccountAuthDTO account,
      @PathVariable long journalUid) {
    journalService.deleteJournal(account.getUid(), journalUid);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * 로그인한 계정의 묵상기록을 등록한다
   *
   * @param journalDTO
   * @return
   */
  @PutMapping
  public ResponseEntity<Object> insertJournal(@RequestBody JournalDTO journalDTO) {
    JournalDTO insertJournal = journalService.insertJournal(journalDTO);
    return new ResponseEntity<>(insertJournal, HttpStatus.OK);
  }

}
