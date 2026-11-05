package com.gospelee.api.enums;

/** 기도 쪽지에서 쓰는 열거형 모음 */
public final class PrayerEnums {

  private PrayerEnums() {
  }

  /** 작성 방식 */
  public enum AuthorMode {ANON_ONLY, NAME_ONLY, AUTHOR_CHOICE}

  /** 작성 대상 */
  public enum WhoCanWrite {APPROVED_ONLY, ALL_MEMBERS}

  /** 공개 범위 설정 */
  public enum ScopeMode {CHURCH_ONLY, GROUP_ALLOWED}

  /** 기도한 사람을 작성자에게 보여 줄지 */
  public enum ResponderVisibility {HIDDEN, NAMED, RESPONDER_CHOICE}

  /** 익명 작성자를 관리자에게 보여 줄지 */
  public enum AdminSeesAnon {NEVER, REPORTED_ONLY, ALWAYS}

  /** 검수 방식 */
  public enum ModerationMode {POST_REPORT, PRE_APPROVAL}

  /** 카드를 올릴 때 고른 표시 방식 */
  public enum DisplayMode {ANON, NAME}

  /** 카드 공개 범위 */
  public enum CardScope {CHURCH, GROUP}

  /** 카드 상태 */
  public enum CardStatus {PENDING, ACTIVE, ANSWERED, HIDDEN}

  /** 정해진 응원 문구 */
  public enum Cheer {WITH_YOU, STRENGTH, PEACE, THANKS}

  /** 신고 처리 상태 */
  public enum ReportStatus {PENDING, RESOLVED, DISMISSED}
}
