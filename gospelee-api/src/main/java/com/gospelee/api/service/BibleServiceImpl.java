package com.gospelee.api.service;

import com.gospelee.api.entity.Bible;
import com.gospelee.api.repository.BibleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BibleServiceImpl implements BibleService {

    private final BibleRepository bibleRepository;

    public BibleServiceImpl(BibleRepository bibleRepository) {
        this.bibleRepository = bibleRepository;
    }

    @Override
    public Optional<List<Bible>> findByBookAndChapter(Integer book, Integer chapter) {
        return bibleRepository.findByBookAndChapter(book, chapter);
    }
}
