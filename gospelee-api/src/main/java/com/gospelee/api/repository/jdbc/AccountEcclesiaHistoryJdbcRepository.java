package com.gospelee.api.repository.jdbc;

import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDetailDTO;
import com.gospelee.api.enums.AccountEcclesiaHistoryStatusType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountEcclesiaHistoryJdbcRepository {

  private final JdbcClient jdbcClient;

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

  public List<AccountEcclesiaHistoryDetailDTO> findByAccountEcclesiaRequestByEcclesiaUid(
      Long ecclesiaUid) {
    String sql = """
        WITH ranked AS (
            SELECT id, account_uid, ecclesia_uid, insert_time, `status`,
                   ROW_NUMBER() OVER (PARTITION BY account_uid ORDER BY insert_time DESC) AS row_num
            FROM account_ecclesia_history
            WHERE ecclesia_uid = :ecclesiaUid
        )
        SELECT
            r.id AS id,
            r.account_uid AS accountUid,
            r.ecclesia_uid AS ecclesiaUid,
            a.name AS `name`,
            a.email AS email,
            a.phone AS phone,
            r.status AS `status`,
            r.insert_time AS insertTime
        FROM ranked r
        JOIN account a ON r.account_uid = a.uid
        WHERE r.row_num = 1
        """;

    return jdbcClient.sql(sql)
        .param("ecclesiaUid", ecclesiaUid)
        .query((rs, rowNum) -> new AccountEcclesiaHistoryDetailDTO(
            rs.getLong("id"),
            rs.getLong("accountUid"),
            rs.getLong("ecclesiaUid"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            AccountEcclesiaHistoryStatusType.of(rs.getString("status")),
            rs.getTimestamp("insertTime").toLocalDateTime()
        ))
        .list();
  }
}
