package com.gospelee.api.controller;

import com.gospelee.api.dto.bible.AccountBibleWriteDTO;
import com.gospelee.api.service.BibleService;
import jakarta.validation.Valid;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/bible")
public class BibleController {

  private final BibleService bibleService;

  private final String REGEX_KOR = ".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*";

  @GetMapping("/view/{book}/{chapter}")
  public ResponseEntity<Object> getBibleByBookAndChapter(@PathVariable(name = "book") Integer book,
      @PathVariable(name = "chapter") Integer chapter) {
    return new ResponseEntity<>(bibleService.findByBookAndChapter(book, chapter)
        .orElseThrow(
            () -> new NoSuchElementException("존재하는 성경이 없습니다 : [" + "book : " + book + "]")),
        HttpStatus.OK);
  }

  @GetMapping("/kor/{short_label}/{chapter}")
  public ResponseEntity<Object> getKorBibleByShortLabelAndChapter(
      @PathVariable(name = "short_label") String short_label,
      @PathVariable(name = "chapter") Integer chapter) {
    if (short_label.matches(REGEX_KOR)) {
      return new ResponseEntity<>(bibleService.findKorByShortLabelAndChapter(short_label, chapter)
          .orElseThrow(() -> new NoSuchElementException(
              "존재하는 성경이 없습니다 : [" + "short_label : " + short_label + "]")), HttpStatus.OK);
    } else {
      // _eng 라벨을 where조건으로 데이터 호출
      return new ResponseEntity<>(HttpStatus.OK);
    }
  }

  @GetMapping("/write/{phone}")
  public ResponseEntity<Object> getBibleWriteByPhone(@PathVariable("phone") String phone) {
    return new ResponseEntity<>(bibleService.findBibleWriteByPhone(phone)
        .orElseThrow(() -> new NoSuchElementException("fail " + "phone : " + phone + "]")),
        HttpStatus.OK);
  }

  /**
   * 성경쓰기 submit에 대한 기록 저장
   *
   * @param dto
   * @return
   */
  @PostMapping("/write/save")
  public ResponseEntity<Object> postBibleWriteByPhone(
      @RequestBody @Valid AccountBibleWriteDTO dto) {
    return new ResponseEntity<>(bibleService.saveBibleWrite(dto)
        .orElseThrow(
            () -> new NoSuchElementException("save fail reason by [" + dto.toString() + "]")),
        HttpStatus.OK);
  }

}
