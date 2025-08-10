package com.gospelee.api.repository.jdbc.ecclesia;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EcclesiaJdbcRepositoryImpl implements EcclesiaJdbcRepository {

  private final JdbcClient jdbcClient;

  @Override
  public List<EcclesiaResponseDTO> findAllWithMasterName() {
    String sql = """
        SELECT e.uid AS uid,
               e.church_identification_number,
               e.status,
               e.name AS `name`,
               a.name AS master_account_name,
               e.senior_paster_name,
               e.church_address,
               e.insert_time
        FROM ecclesia e
        LEFT JOIN account a ON e.master_account_uid = a.uid
        """;

    return jdbcClient.sql(sql)
        .query((rs, rowNum) -> new EcclesiaResponseDTO(
            rs.getLong("uid"),
            rs.getString("church_identification_number"),
            rs.getString("status"),
            rs.getString("name"),
            rs.getString("master_account_name"),
            rs.getString("senior_paster_name"),
            rs.getString("church_address"),
            rs.getTimestamp("insert_time").toLocalDateTime()
        ))
        .list();
  }

  @Override
  public List<EcclesiaResponseDTO> searchEcclesia(String text) {
    String sql = """
        SELECT e.uid AS uid,
               e.church_identification_number,
               e.status,
               e.name AS `name`,
               e.senior_paster_name,
               e.church_address,
               e.insert_time
        FROM ecclesia e
        WHERE e.name LIKE :keyword
        """;

    return jdbcClient.sql(sql)
        .param("keyword", "%" + text + "%")
        .query((rs, rowNum) -> new EcclesiaResponseDTO(
            rs.getLong("uid"),
            rs.getString("church_identification_number"),
            rs.getString("status"),
            rs.getString("name"),
            null,
            rs.getString("senior_paster_name"),
            rs.getString("church_address"),
            rs.getTimestamp("insert_time").toLocalDateTime()
        ))
        .list();
  }
}
