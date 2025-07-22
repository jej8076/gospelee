package com.gospelee.api.repository.jdbc.ecclesia;

import com.gospelee.api.dto.ecclesia.AccountEcclesiaHistoryDTO;
import java.util.List;

public interface AccountEcclesiaHistoryJdbcRepository {

  List<AccountEcclesiaHistoryDTO> findByStatusAndEcclesiaId(Long ecclesiaUid);
}
