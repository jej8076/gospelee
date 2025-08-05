package com.gospelee.api.repository.jdbc.ecclesia;

import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDetailDTO;
import java.util.List;

public interface AccountEcclesiaHistoryJdbcRepository {

  List<AccountEcclesiaHistoryDTO> findByStatusAndEcclesiaId(Long ecclesiaUid);

  List<AccountEcclesiaHistoryDetailDTO> findByAccountEcclesiaRequestByEcclesiaUid(Long ecclesiaUid);
}
