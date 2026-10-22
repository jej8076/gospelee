package com.gospelee.api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 통독 목표에서 "이어서 읽기" 위치를 계산한다.
 *
 * <p>책 순서는 앱의 진도 체크 목록(범위 + 순서 프리셋)과 같은 규칙을 따른다.
 * CHRONOLOGICAL 은 앱에서도 별도 순서가 없어 성경목차순과 같게 취급한다.
 */
public final class BibleReadingContinue {

  private BibleReadingContinue() {
  }

  /** 읽을 위치 (책 번호, 장 번호) */
  public record Position(int book, int chapter) {

  }

  /** 읽음 기록 집합에서 쓰는 키 */
  public static int key(int book, int chapter) {
    return book * 1000 + chapter;
  }

  /** 목표 범위/순서에 따른 책 읽는 순서 */
  public static List<Integer> orderedBooks(String rangeType, String orderType, String customBooks) {
    String range = rangeType == null ? "ALL" : rangeType.toUpperCase();
    String order = orderType == null ? "CANONICAL" : orderType.toUpperCase();
    List<Integer> custom = parseBooks(customBooks);

    List<Integer> base = new ArrayList<>();
    switch (range) {
      case "OLD":
        for (int b = 1; b <= BibleUtils.OLD_TESTAMENT_BOOKS; b++) {
          base.add(b);
        }
        break;
      case "NEW":
        for (int b = BibleUtils.OLD_TESTAMENT_BOOKS + 1; b <= 66; b++) {
          base.add(b);
        }
        break;
      case "CUSTOM":
        base.addAll(custom);
        break;
      default:
        for (int b = 1; b <= 66; b++) {
          base.add(b);
        }
        break;
    }

    if ("NEW_FIRST".equals(order)) {
      List<Integer> reordered = new ArrayList<>();
      base.stream().filter(b -> b > BibleUtils.OLD_TESTAMENT_BOOKS).forEach(reordered::add);
      base.stream().filter(b -> b <= BibleUtils.OLD_TESTAMENT_BOOKS).forEach(reordered::add);
      return reordered;
    }
    if ("CUSTOM".equals(order) && !"CUSTOM".equals(range) && !custom.isEmpty()) {
      Set<Integer> baseSet = new LinkedHashSet<>(base);
      return custom.stream().filter(baseSet::contains).collect(Collectors.toList());
    }
    return base;
  }

  /**
   * 마지막으로 체크한 장 다음부터 순서대로 훑어 처음 만나는 안 읽은 장을 반환한다.
   * 끝까지 가면 목표 범위의 처음으로 돌아가 안 읽은 장을 찾고, 모두 읽었으면 null.
   *
   * @param lastBook 마지막으로 체크한 책 (기록이 없으면 null)
   * @param lastChapter 마지막으로 체크한 장
   * @param readKeys 이미 읽은 장의 {@link #key(int, int)} 집합
   */
  public static Position findContinuePosition(List<Integer> orderedBooks, Integer lastBook,
      Integer lastChapter, Set<Integer> readKeys) {
    List<Position> positions = new ArrayList<>();
    for (int book : orderedBooks) {
      int chapters = BibleUtils.getChaptersByBook(book);
      for (int chapter = 1; chapter <= chapters; chapter++) {
        positions.add(new Position(book, chapter));
      }
    }
    if (positions.isEmpty()) {
      return null;
    }

    int start = 0;
    if (lastBook != null && lastChapter != null) {
      for (int i = 0; i < positions.size(); i++) {
        Position p = positions.get(i);
        if (p.book() == lastBook && p.chapter() == lastChapter) {
          start = i + 1;
          break;
        }
      }
    }

    for (int i = 0; i < positions.size(); i++) {
      Position p = positions.get((start + i) % positions.size());
      if (!readKeys.contains(key(p.book(), p.chapter()))) {
        return p;
      }
    }
    return null;
  }

  private static List<Integer> parseBooks(String customBooks) {
    if (customBooks == null || customBooks.isBlank()) {
      return new ArrayList<>();
    }
    return Arrays.stream(customBooks.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Integer::parseInt)
        .filter(b -> b >= 1 && b <= 66)
        .collect(Collectors.toList());
  }
}
