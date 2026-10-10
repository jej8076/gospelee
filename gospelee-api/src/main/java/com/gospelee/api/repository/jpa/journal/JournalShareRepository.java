package com.gospelee.api.repository.jpa.journal;

import com.gospelee.api.entity.JournalShare;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalShareRepository extends JpaRepository<JournalShare, Long> {

  Optional<JournalShare> findByToken(String token);

  Optional<JournalShare> findByJournalUid(Long journalUid);

  List<JournalShare> findByJournalUidIn(List<Long> journalUids);
}
