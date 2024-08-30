package com.gospelee.api.repository;

import com.gospelee.api.dto.journal.JournalDTO;
import com.gospelee.api.entity.Journal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalRepository extends JpaRepository<Journal, String> {

  List<Journal> findByAccountUid(long accountUid);

}
