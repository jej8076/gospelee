package com.gospelee.api.controller;

import com.gospelee.api.service.BibleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/bible")
public class BibleController {

    private final BibleService bibleService;

    private final String REGEX_KOR = ".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*";

    @GetMapping("/{book}/{chapter}")
    public ResponseEntity<Object> getBibleBy(@PathVariable(name = "book") Integer book, @PathVariable(name = "chapter") Integer chapter) {
        return new ResponseEntity<>(bibleService.findByBookAndChapter(book, chapter)
                .orElseThrow(() -> new NoSuchElementException("존재하는 성경이 없습니다 : [" + "book : " + book + "]")), HttpStatus.OK);
    }

    @GetMapping("/kor/{short_label}/{chapter}")
    public ResponseEntity<Object> getBibleBy(@PathVariable(name = "short_label") String short_label, @PathVariable(name = "chapter") Integer chapter) {
        if (short_label.matches(REGEX_KOR)) {
            return new ResponseEntity<>(bibleService.findKorByShortLabelAndChapter(short_label, chapter)
                    .orElseThrow(() -> new NoSuchElementException("존재하는 성경이 없습니다 : [" + "short_label : " + short_label + "]")), HttpStatus.OK);
        } else {
            // _eng 라벨을 where조건으로 데이터 호출
            return new ResponseEntity<>(HttpStatus.OK);
        }

    }

}