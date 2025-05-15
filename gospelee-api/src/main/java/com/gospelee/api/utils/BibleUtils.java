package com.gospelee.api.utils;

public class BibleUtils {

  /**
   * 1 : 구약, 2 : 신약
   *
   * @param book
   * @return
   */
  public static int getCateByBook(int book) {
    // 1~39 -> 구약, 40~66 -> 신약
    return book > 39 ? 2 : 1;
  }
}
