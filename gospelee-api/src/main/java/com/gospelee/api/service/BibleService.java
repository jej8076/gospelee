package com.gospelee.api.service;

import com.gospelee.api.entity.Bible;

import java.util.List;
import java.util.Optional;

public interface BibleService {
    Optional<List<Bible>> findByBookAndChapter(Integer book, Integer chapter);
}
