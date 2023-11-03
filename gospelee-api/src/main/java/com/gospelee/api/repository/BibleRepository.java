package com.gospelee.api.repository;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Bible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BibleRepository extends JpaRepository<Bible, String> {
    Optional<List<Bible>> findByBookAndChapter(Integer book, Integer chapter);
}
