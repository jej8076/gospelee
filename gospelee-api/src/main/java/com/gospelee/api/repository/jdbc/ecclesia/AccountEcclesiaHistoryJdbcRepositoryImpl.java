package com.gospelee.api.repository.jdbc.ecclesia;

import com.gospelee.api.dto.ecclesia.AccountEcclesiaHistoryDTO;
import com.gospelee.api.enums.AccountEcclesiaHistoryStatusType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountEcclesiaHistoryJdbcRepositoryImpl implements
    AccountEcclesiaHistoryJdbcRepository {

  private final JdbcClient jdbcClient;

  @Override
  public List<AccountEcclesiaHistoryDTO> findByStatusAndEcclesiaId(Long ecclesiaUid) {
    String sql = """
        SELECT id, account_uid, ecclesia_uid, insert_time, `status`
        FROM (
            SELECT
                id,
                account_uid,
                ecclesia_uid,
                insert_time,
                `status`,
                ROW_NUMBER() OVER(PARTITION BY ecclesia_uid ORDER BY insert_time DESC) as row_num
            FROM
                account_ecclesia_history
            WHERE ecclesia_uid = :ecclesiaUid
        ) AS subquery
        WHERE row_num = 1;
        """;

    return jdbcClient.sql(sql)
        .param("ecclesiaUid", ecclesiaUid)
        .query((rs, rowNum) -> new AccountEcclesiaHistoryDTO(
            rs.getLong("id"),
            rs.getLong("account_uid"),
            rs.getLong("ecclesia_uid"),
            AccountEcclesiaHistoryStatusType.of(rs.getString("status")),
            rs.getTimestamp("insert_time").toLocalDateTime()
        ))
        .list();
  }
}
