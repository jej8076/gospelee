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

    @GetMapping("/kor/{book}/{chapter}")
    public ResponseEntity<Object> getBibleBy(@PathVariable(name = "book") String short_label, @PathVariable(name = "chapter") Integer chapter) {
        if (short_label.matches(REGEX_KOR)) {
            return new ResponseEntity<>(bibleService.findKorByShortLabelAndChapter(short_label, chapter)
                    .orElseThrow(() -> new NoSuchElementException("존재하는 성경이 없습니다 : [" + "short_label : " + short_label + "]")), HttpStatus.OK);
        } else {

        }

    }

//    @PostMapping("")
//    public ResponseEntity<Object> saveAccount(final @RequestBody @Valid AccountVo accountVo) throws IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
//        Account account = (Account) FieldUtil.toEntity(accountVo);
//        accountService.createAccount(account);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

}