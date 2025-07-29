package com.gospelee.api.repository.jpa.journal;

import com.gospelee.api.entity.Journal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalRepository extends JpaRepository<Journal, Long> {

  @Query("SELECT DISTINCT j FROM Journal j LEFT JOIN FETCH j.JournalBibleList WHERE j.accountUid = :accountUid ORDER BY j.insertTime DESC")
  List<Journal> findByAccountUidWithJournalBibles(@Param("accountUid") long accountUid);

}
