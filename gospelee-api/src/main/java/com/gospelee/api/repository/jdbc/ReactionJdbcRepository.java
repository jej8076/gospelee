package com.gospelee.api.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReactionJdbcRepository {

  private final JdbcClient jdbcClient;

  /**
   * 반응 추가. 이미 있으면 무시하고 0, 새로 들어갔으면 1 을 돌려준다 (PostgreSQL).
   * 동시에 같은 요청이 와도 유니크 제약이 걸러서 정확히 한 번만 1 이 나온다.
   */
  public int insertIgnore(String reactionType, String targetType, Long targetId, Long accountUid) {
    return jdbcClient.sql("""
            INSERT INTO reaction (reaction_type, target_type, target_id, account_uid, insert_time)
            VALUES (:reactionType, :targetType, :targetId, :accountUid, now())
            ON CONFLICT (reaction_type, target_type, target_id, account_uid) DO NOTHING
            """)
        .param("reactionType", reactionType)
        .param("targetType", targetType)
        .param("targetId", targetId)
        .param("accountUid", accountUid)
        .update();
  }
}
