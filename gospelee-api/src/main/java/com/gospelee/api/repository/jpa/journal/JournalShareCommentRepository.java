package com.gospelee.api.repository.jpa.journal;

import com.gospelee.api.entity.JournalShareComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalShareCommentRepository extends JpaRepository<JournalShareComment, Long> {

  List<JournalShareComment> findByShareUidAndStatusOrderByUidAsc(Long shareUid, String status);

  long countByShareUidAndStatus(Long shareUid, String status);
}
