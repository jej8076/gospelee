package com.gospelee.api.repository;

import com.gospelee.api.dto.ecclesia.AccountEcclesiaHistoryDTO;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.repository.jdbc.ecclesia.AccountEcclesiaHistoryJdbcRepository;
import com.gospelee.api.repository.jpa.ecclesia.AccountEcclesiaHistoryJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountEcclesiaHistoryRepositoryImpl implements AccountEcclesiaHistoryRepository {

  private final AccountEcclesiaHistoryJpaRepository accountEcclesiaHistoryJpaRepository;
  private final AccountEcclesiaHistoryJdbcRepository accountEcclesiaHistoryJdbcRepository;

  @Override
  public AccountEcclesiaHistory save(AccountEcclesiaHistory accountEcclesiaHistory) {
    return accountEcclesiaHistoryJpaRepository.save(accountEcclesiaHistory);
  }

  @Override
  public List<AccountEcclesiaHistoryDTO> findByStatusAndEcclesiaId(Long ecclesiaUid) {
    return accountEcclesiaHistoryJdbcRepository.findByStatusAndEcclesiaId(ecclesiaUid);
  }
}
