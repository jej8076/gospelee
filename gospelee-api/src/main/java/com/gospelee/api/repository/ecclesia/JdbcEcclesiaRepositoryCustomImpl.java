package com.gospelee.api.repository.ecclesia;

import com.gospelee.api.dto.ecclesia.EcclesiaResponseDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("jdbcEcclesiaRepository")
@RequiredArgsConstructor
public class JdbcEcclesiaRepositoryCustomImpl implements EcclesiaRepositoryCustom {

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Override
  public List<EcclesiaResponseDTO> findAllWithMasterName() {
    String sql = """
        SELECT e.uid AS ecclesia_uid,
               e.church_identification_number,
               e.status,
               e.name AS ecclesia_name,
               a.name AS master_account_name,
               e.insert_time
        FROM ecclesia e
        LEFT JOIN account a ON e.master_account_uid = a.uid
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> new EcclesiaResponseDTO(
        rs.getLong("ecclesia_uid"),
        rs.getString("church_identification_number"),
        rs.getString("status"),
        rs.getString("ecclesia_name"),
        rs.getString("master_account_name"),
        rs.getTimestamp("insert_time").toLocalDateTime()
    ));
  }

  @Override
  public List<EcclesiaResponseDTO> searchEcclesia(String text) {
    String sql = """
        SELECT e.uid AS ecclesia_uid,
               e.church_identification_number,
               e.status,
               e.name AS ecclesia_name,
               e.insert_time
        FROM ecclesia e
        WHERE e.name LIKE :keyword
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("keyword", "%" + text + "%");

    return namedParameterJdbcTemplate.query(
        sql,
        params,
        (rs, rowNum) -> new EcclesiaResponseDTO(
            rs.getLong("ecclesia_uid"),
            rs.getString("church_identification_number"),
            rs.getString("status"),
            rs.getString("ecclesia_name"),
            null,
            rs.getTimestamp("insert_time").toLocalDateTime()
        ));
  }
}
