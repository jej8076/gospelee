package com.gospelee.api.repository;

import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDetailDTO;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import java.util.List;

public interface AccountEcclesiaHistoryRepository {

  AccountEcclesiaHistory save(AccountEcclesiaHistory accountEcclesiaHistory);

  AccountEcclesiaHistory findById(long id);

  List<AccountEcclesiaHistoryDTO> findByStatusAndEcclesiaId(Long ecclesiaUid);

  List<AccountEcclesiaHistoryDetailDTO> findByAccountEcclesiaRequestByEcclesiaUid(Long ecclesiaUid);
}
