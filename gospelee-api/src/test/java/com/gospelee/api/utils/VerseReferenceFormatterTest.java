package com.gospelee.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gospelee.api.utils.VerseReferenceFormatter.VerseLine;
import java.util.List;
import org.junit.jupiter.api.Test;

class VerseReferenceFormatterTest {

  private static VerseLine v(int book, String label, int chapter, int verse) {
    return new VerseLine(book, label, chapter, verse, "본문" + verse);
  }

  @Test
  void 연속된_절은_범위로_묶고_떨어진_절은_쉼표로_구분한다() {
    List<VerseLine> lines = List.of(v(43, "요한복음", 3, 20), v(43, "요한복음", 3, 16),
        v(43, "요한복음", 3, 17), v(43, "요한복음", 3, 18));

    assertEquals("요한복음 3:16-18, 20", VerseReferenceFormatter.reference(lines));
  }

  @Test
  void 장이나_책이_다르면_슬래시로_구분하고_성경_순서로_정렬한다() {
    List<VerseLine> lines = List.of(v(43, "요한복음", 1, 1), v(1, "창세기", 1, 1),
        v(43, "요한복음", 3, 16));

    assertEquals("창세기 1:1 / 요한복음 1:1 / 요한복음 3:16",
        VerseReferenceFormatter.reference(lines));
  }

  @Test
  void 본문은_절_번호와_함께_줄바꿈으로_이어진다() {
    List<VerseLine> lines = List.of(v(43, "요한복음", 3, 17), v(43, "요한복음", 3, 16));

    assertEquals("16 본문16\n17 본문17", VerseReferenceFormatter.text(lines));
  }

  @Test
  void 구절이_없으면_빈_문자열() {
    assertEquals("", VerseReferenceFormatter.reference(List.of()));
    assertEquals("", VerseReferenceFormatter.text(List.of()));
  }
}
