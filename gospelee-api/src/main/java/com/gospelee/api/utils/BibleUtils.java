package com.gospelee.api.utils;

public class BibleUtils {

  // 성경 66권 총 장 수 (1-66번 책 순서)
  public static final int[] BOOK_CHAPTERS = {
      50, 40, 27, 36, 34, 24, 21, 4, 31, 24,  // 창세기~사무엘하 (1~10)
      22, 25, 29, 36, 10, 13, 10, 42, 150, 31, // 열왕기상~잠언 (11~20)
      12, 8, 66, 52, 5, 48, 12, 14, 3, 9,     // 전도서~아모스 (21~30)
      1, 4, 7, 3, 3, 3, 2, 14, 4,             // 오바댜~말라기 (31~39)
      28, 16, 24, 21, 28, 16, 16, 13, 6, 6,   // 마태복음~에베소서 (40~49)
      4, 4, 5, 3, 6, 4, 3, 1, 13, 5,          // 빌립보서~야고보서 (50~59)
      5, 3, 5, 1, 1, 1, 22                    // 베드로전서~요한계시록 (60~66)
  };

  // 성경 66권 이름
  public static final String[] BOOK_NAMES = {
      "창세기", "출애굽기", "레위기", "민수기", "신명기", "여호수아", "사사기", "룻기", "사무엘상", "사무엘하",
      "열왕기상", "열왕기하", "역대상", "역대하", "에스라", "느헤미야", "에스더", "욥기", "시편", "잠언",
      "전도서", "아가", "이사야", "예레미야", "예레미야애가", "에스겔", "다니엘", "호세아", "요엘", "아모스",
      "오바댜", "요나", "미가", "나훔", "하박국", "스바냐", "학개", "스가랴", "말라기",
      "마태복음", "마가복음", "누가복음", "요한복음", "사도행전", "로마서", "고린도전서", "고린도후서", "갈라디아서", "에베소서",
      "빌립보서", "골로새서", "데살로니가전서", "데살로니가후서", "디모데전서", "디모데후서", "디도서", "빌레몬서", "히브리서", "야고보서",
      "베드로전서", "베드로후서", "요한일서", "요한이서", "요한삼서", "유다서", "요한계시록"
  };

  public static final int TOTAL_CHAPTERS = 1189;
  public static final int OLD_TESTAMENT_BOOKS = 39;
  public static final int OLD_TESTAMENT_CHAPTERS = 929;
  public static final int NEW_TESTAMENT_CHAPTERS = 260;

  /**
   * 1 : 구약, 2 : 신약
   *
   * @param book
   * @return
   */
  public static int getCateByBook(int book) {
    // 1~39 -> 구약, 40~66 -> 신약
    return book > OLD_TESTAMENT_BOOKS ? 2 : 1;
  }

  public static int getChaptersByBook(int book) {
    if (book < 1 || book > 66) {
      return 0;
    }
    return BOOK_CHAPTERS[book - 1];
  }

  public static String getBookName(int book) {
    if (book < 1 || book > 66) {
      return "";
    }
    return BOOK_NAMES[book - 1];
  }
}
