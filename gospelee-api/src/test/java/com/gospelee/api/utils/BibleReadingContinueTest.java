package com.gospelee.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.gospelee.api.utils.BibleReadingContinue.Position;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BibleReadingContinueTest {

  private static final List<Integer> ALL = BibleReadingContinue.orderedBooks("ALL", "CANONICAL", null);

  private static Set<Integer> read(int book, int fromChapter, int toChapter) {
    Set<Integer> keys = new HashSet<>();
    for (int c = fromChapter; c <= toChapter; c++) {
      keys.add(BibleReadingContinue.key(book, c));
    }
    return keys;
  }

  @Test
  void 기록이_없으면_범위의_첫_장() {
    assertEquals(new Position(1, 1),
        BibleReadingContinue.findContinuePosition(ALL, null, null, new HashSet<>()));
  }

  @Test
  void 마지막_체크_다음_장으로_이어간다() {
    assertEquals(new Position(1, 4),
        BibleReadingContinue.findContinuePosition(ALL, 1, 3, read(1, 1, 3)));
  }

  @Test
  void 책의_마지막_장이면_다음_책_1장() {
    assertEquals(new Position(2, 1),
        BibleReadingContinue.findContinuePosition(ALL, 1, 50, read(1, 1, 50)));
  }

  @Test
  void 다음_장이_이미_읽음이면_안_읽은_장까지_건너뛴다() {
    Set<Integer> keys = read(1, 1, 5);
    assertEquals(new Position(1, 6), BibleReadingContinue.findContinuePosition(ALL, 1, 3, keys));
  }

  @Test
  void 마지막이_앞쪽이어도_그_뒤의_안_읽은_장을_찾는다() {
    // 창세기 10장을 나중에 체크했어도 마지막 체크가 10장이면 11장부터
    Set<Integer> keys = read(1, 1, 3);
    keys.add(BibleReadingContinue.key(1, 10));
    assertEquals(new Position(1, 11), BibleReadingContinue.findContinuePosition(ALL, 1, 10, keys));
  }

  @Test
  void 끝까지_가면_처음으로_돌아가_안_읽은_장을_찾는다() {
    Set<Integer> keys = new HashSet<>();
    for (int b = 1; b <= 66; b++) {
      keys.addAll(read(b, 1, BibleUtils.getChaptersByBook(b)));
    }
    keys.remove(BibleReadingContinue.key(1, 1));
    assertEquals(new Position(1, 1), BibleReadingContinue.findContinuePosition(ALL, 66, 22, keys));
  }

  @Test
  void 모두_읽었으면_null() {
    Set<Integer> keys = new HashSet<>();
    for (int b = 1; b <= 66; b++) {
      keys.addAll(read(b, 1, BibleUtils.getChaptersByBook(b)));
    }
    assertNull(BibleReadingContinue.findContinuePosition(ALL, 66, 22, keys));
  }

  @Test
  void 신약_범위는_마태복음부터() {
    List<Integer> nt = BibleReadingContinue.orderedBooks("NEW", "CANONICAL", null);
    assertEquals(27, nt.size());
    assertEquals(new Position(40, 1),
        BibleReadingContinue.findContinuePosition(nt, null, null, new HashSet<>()));
    assertEquals(new Position(41, 1),
        BibleReadingContinue.findContinuePosition(nt, 40, 28, read(40, 1, 28)));
  }

  @Test
  void 신약_우선이면_신약_끝_다음은_창세기() {
    List<Integer> order = BibleReadingContinue.orderedBooks("ALL", "NEW_FIRST", null);
    assertEquals(40, order.get(0));
    assertEquals(new Position(1, 1),
        BibleReadingContinue.findContinuePosition(order, 66, 22, read(66, 1, 22)));
  }

  @Test
  void 직접_선택_범위는_지정한_순서를_따른다() {
    List<Integer> order = BibleReadingContinue.orderedBooks("CUSTOM", "CUSTOM", "45, 19");
    assertEquals(List.of(45, 19), order);
    assertEquals(new Position(19, 1),
        BibleReadingContinue.findContinuePosition(order, 45, 16, read(45, 1, 16)));
  }

  @Test
  void 순서만_직접_지정하면_범위와_교집합으로_정렬한다() {
    assertEquals(List.of(19, 1),
        BibleReadingContinue.orderedBooks("ALL", "CUSTOM", "19,1"));
    assertEquals(List.of(40),
        BibleReadingContinue.orderedBooks("NEW", "CUSTOM", "19,40"));
  }

  @Test
  void 마지막_기록이_범위_밖이면_처음부터_찾는다() {
    List<Integer> nt = BibleReadingContinue.orderedBooks("NEW", "CANONICAL", null);
    assertEquals(new Position(40, 1),
        BibleReadingContinue.findContinuePosition(nt, 1, 5, new HashSet<>()));
  }
}
