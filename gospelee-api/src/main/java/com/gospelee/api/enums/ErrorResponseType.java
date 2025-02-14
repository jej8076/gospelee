package com.gospelee.api.enums;

/**
 * <pre>
 * controller 파일 이름 기준으로 code 를 나눈다
 * code : front에 에러코드 전달에 사용
 * message : 개발자가 에러에 대해 코드를 보지 않고 이해할 수 있도록 정의하는 용도 (log등)
 * </pre>
 */
public enum ErrorResponseType {
  /* 공통 에러 코드 */
  COMM_101("COMM-101", "테이블에 해당 데이터가 존재하지 않음"),
  COMM_102("COMM-102", "비정상적인 접근이 시도됨"),
  COMM_999("COMM-999", "예상치 못한 에러 발생"),

  /* AUTH */
  AUTH_101("AUTH-101", "로그인에 5회 실패 등으로 계정이 정지됨"),
  AUTH_102("AUTH-102", "아이디가 없거나 패스워드가 일치하지 않음"),
  AUTH_103("AUTH-103", "토큰 유효성 검사 실패"),
  AUTH_104("AUTH-104", "인증번호 불일치"),
  AUTH_105("AUTH-105", "쿠키 생성에 허용되지 않은 도메인"),
  AUTH_106("AUTH-106", "비밀번호 변경 실패. 이전 비밀번호와 일치함"),
  AUTH_107("AUTH-107", "비밀번호 변경 실패. 변경 전 비밀번호가 현재 비밀번호와 일치하지 않음"),
  AUTH_108("AUTH-108", "허용되지 않은 아이피"),
  AUTH_109("AUTH-109", "중복로그인으로 인한 로그아웃"),
  AUTH_110("AUTH-110", "토큰이 존재하지 않음"),

  /* ECCLESIA */
  ECCL_101("ECCL-101", "교회 정보가 없음"),
  ECCL_102("ECCL-102", "교회 정보가 있지만 교회가 승인되지 않음"),
  ;

  private final String code;
  private final String message;

  ErrorResponseType(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String code() {
    return code;
  }

  public String message() {
    return message;
  }

  public String print() {
    return "code=" + this.code + ", message=" + this.message;
  }

  public String print(String info) {
    return "code=" + this.code + ", message=" + this.message + ", (" + info + ")";
  }
}
