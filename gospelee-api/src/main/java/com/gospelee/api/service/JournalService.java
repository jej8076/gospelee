package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.journal.JournalDTO;
import com.gospelee.api.entity.Account;
import java.util.List;

public interface JournalService {

  List<JournalDTO> getJournalList(long accountUid);

  JournalDTO insertJournal(AccountAuthDTO account, JournalDTO journalDTO);

}
