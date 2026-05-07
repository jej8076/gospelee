package com.gospelee.api.service;

import com.gospelee.api.dto.auth.SessionData;
import java.util.Optional;

public interface SessionService {

  /**
   * 새 세션을 생성하고 세션 ID를 반환한다.
   *
   * @param data 세션에 저장할 사용자 정보
   * @return 세션 ID (cookie 값으로 사용)
   */
  String create(SessionData data);

  /**
   * 세션 ID로 세션 데이터를 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 세션 데이터 (없으면 Optional.empty)
   */
  Optional<SessionData> get(String sessionId);

  /**
   * 세션 ID로 세션을 무효화한다.
   *
   * @param sessionId 세션 ID
   */
  void delete(String sessionId);
}
