package com.gospelee.api.repository;

import com.gospelee.api.dto.ecclesia.AccountEcclesiaHistoryDTO;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import java.util.List;

public interface AccountEcclesiaHistoryRepository {

  AccountEcclesiaHistory save(AccountEcclesiaHistory accountEcclesiaHistory);

  List<AccountEcclesiaHistoryDTO> findByStatusAndEcclesiaId(Long ecclesiaUid);
}
