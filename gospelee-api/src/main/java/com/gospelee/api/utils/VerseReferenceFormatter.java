package com.gospelee.api.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 묵상에 연결된 구절을 "요한복음 3:16-18, 20" 형태의 표기와 본문 텍스트로 만든다 */
public final class VerseReferenceFormatter {

  public record VerseLine(int book, String bookLabel, int chapter, int verse, String sentence) {

  }

  private VerseReferenceFormatter() {
  }

  public static String reference(List<VerseLine> lines) {
    Map<String, List<Integer>> grouped = new LinkedHashMap<>();
    for (VerseLine line : sorted(lines)) {
      String key = line.bookLabel() + " " + line.chapter();
      grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(line.verse());
    }

    List<String> parts = new ArrayList<>();
    grouped.forEach((key, verses) -> parts.add(key + ":" + ranges(verses)));
    return String.join(" / ", parts);
  }

  public static String text(List<VerseLine> lines) {
    StringBuilder sb = new StringBuilder();
    for (VerseLine line : sorted(lines)) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(line.verse()).append(' ').append(line.sentence() == null ? "" : line.sentence());
    }
    return sb.toString();
  }

  private static List<VerseLine> sorted(List<VerseLine> lines) {
    return lines.stream()
        .sorted(Comparator.comparingInt(VerseLine::book)
            .thenComparingInt(VerseLine::chapter)
            .thenComparingInt(VerseLine::verse))
        .toList();
  }

  /** [16,17,18,20] -> "16-18, 20" */
  private static String ranges(List<Integer> verses) {
    List<String> out = new ArrayList<>();
    int start = verses.get(0);
    int prev = start;
    for (int i = 1; i <= verses.size(); i++) {
      boolean end = i == verses.size();
      int current = end ? 0 : verses.get(i);
      if (end || current != prev + 1) {
        out.add(start == prev ? String.valueOf(start) : start + "-" + prev);
        start = current;
      }
      prev = current;
    }
    return String.join(", ", out);
  }
}
