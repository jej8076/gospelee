package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.journal.JournalBibleDTO;
import com.gospelee.api.dto.journal.JournalDTO;
import com.gospelee.api.entity.Journal;
import com.gospelee.api.entity.JournalBible;
import com.gospelee.api.repository.jpa.JournalRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

  private final JournalRepository journalRepository;

  public List<JournalDTO> getJournalList(long accountUid) {
    return JournalDTO.toDtoList(journalRepository.findByAccountUid(accountUid));
  }

  @Override
  public JournalDTO insertJournal(AccountAuthDTO account, JournalDTO journalDTO) {
    Journal journal = Journal.builder()
        .accountUid(account.getUid())
        .content(journalDTO.getContent())
        .build();

    List<JournalBible> journalBibleList = new ArrayList<>();
    for (JournalBibleDTO journalBibleDTO : journalDTO.getJournalBibleList()) {
      JournalBible journalBible = JournalBible.builder()
          .journal(journal)
          .book(journalBibleDTO.getBook())
          .chapter(journalBibleDTO.getChapter())
          .verse(journalBibleDTO.getVerse())
          .build();
      journalBibleList.add(journalBible);
    }
    journal.changeJournalBibleList(journalBibleList);

    return JournalDTO.toDto(journalRepository.save(journal));
  }


}


