package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.journal.JournalBibleDTO;
import com.gospelee.api.dto.journal.JournalDTO;
import com.gospelee.api.entity.Journal;
import com.gospelee.api.entity.JournalBible;
import com.gospelee.api.repository.jpa.journal.JournalRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

  private final JournalRepository journalRepository;
  private final JournalShareService journalShareService;

  public List<JournalDTO> getJournalList(long accountUid) {
    return JournalDTO.toDtoList(journalRepository.findByAccountUidWithJournalBibles(accountUid));
  }

  @Override
  public JournalDTO insertJournal(JournalDTO journalDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    Journal journal;

    // 업데이트인 경우
    if (journalDTO.getUid() != null) {
      journal = journalRepository.findById(journalDTO.getUid())
          .orElseThrow(() -> new RuntimeException("Journal not found"));

      // 본인 묵상만 수정할 수 있다
      if (!journal.getAccountUid().equals(account.getUid())) {
        throw new AccessDeniedException("본인의 묵상만 수정할 수 있습니다.");
      }

      // Journal의 content만 업데이트 (JournalBible은 변경하지 않음)
      journal.changeContent(journalDTO.getContent());
      return JournalDTO.toDto(journalRepository.save(journal));
    }

    // 새로 생성하는 경우
    journal = Journal.builder()
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

  @Override
  @Transactional
  public void deleteJournal(long accountUid, long journalUid) {
    Journal journal = journalRepository.findById(journalUid)
        .orElseThrow(() -> new RuntimeException("Journal not found"));
    if (!journal.getAccountUid().equals(accountUid)) {
      throw new AccessDeniedException("본인의 묵상만 삭제할 수 있습니다.");
    }
    journalShareService.revokeByJournal(journalUid);
    journalRepository.delete(journal);
  }

}


