package com.gospelee.api.repository.jpa.journal;

import com.gospelee.api.entity.JournalShareReceived;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalShareReceivedRepository extends JpaRepository<JournalShareReceived, Long> {

  Optional<JournalShareReceived> findByAccountUidAndShareUid(Long accountUid, Long shareUid);

  List<JournalShareReceived> findByAccountUidOrderByLastViewedAtDesc(Long accountUid);

  void deleteByAccountUid(Long accountUid);
}
