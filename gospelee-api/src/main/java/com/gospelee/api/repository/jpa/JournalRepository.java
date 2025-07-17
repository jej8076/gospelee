package com.gospelee.api.repository.jpa;

import com.gospelee.api.entity.Journal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalRepository extends JpaRepository<Journal, Long> {

  List<Journal> findByAccountUid(long accountUid);

}
