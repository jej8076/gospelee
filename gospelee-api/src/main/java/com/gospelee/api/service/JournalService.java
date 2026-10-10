package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.journal.JournalDTO;
import com.gospelee.api.entity.Account;
import java.util.List;

public interface JournalService {

  List<JournalDTO> getJournalList(long accountUid);

  JournalDTO insertJournal(JournalDTO journalDTO);

  /** 내 묵상을 삭제한다. 공유 중이면 공유도 함께 닫는다 */
  void deleteJournal(long accountUid, long journalUid);

}
