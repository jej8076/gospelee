package com.gospelee.api.repository;

import com.gospelee.api.entity.Bible;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BibleRepository extends JpaRepository<Bible, String> {

  Optional<List<Bible>> findByBookAndChapter(Integer book, Integer chapter);

  Optional<List<Bible>> findByShortLabelAndChapter(String short_label, Integer chapter);
}
