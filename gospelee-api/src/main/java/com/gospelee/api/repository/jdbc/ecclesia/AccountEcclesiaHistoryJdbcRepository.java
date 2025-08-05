package com.gospelee.api.repository.jdbc.ecclesia;

import com.gospelee.api.dto.ecclesia.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.ecclesia.AccountEcclesiaHistoryDetailDTO;
import java.util.List;

public interface AccountEcclesiaHistoryJdbcRepository {

  List<AccountEcclesiaHistoryDTO> findByStatusAndEcclesiaId(Long ecclesiaUid);

  List<AccountEcclesiaHistoryDetailDTO> findByAccountEcclesiaRequestByEcclesiaUid(Long ecclesiaUid);
}
