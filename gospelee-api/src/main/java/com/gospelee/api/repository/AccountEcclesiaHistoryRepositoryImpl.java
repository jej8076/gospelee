package com.gospelee.api.repository;

import com.gospelee.api.dto.account.AccountEcclesiaHistoryDTO;
import com.gospelee.api.dto.account.AccountEcclesiaHistoryDetailDTO;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.repository.jdbc.AccountEcclesiaHistoryJdbcRepository;
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
  public AccountEcclesiaHistory findById(long id) {
    return accountEcclesiaHistoryJpaRepository.findById(id).orElse(null);
  }

  @Override
  public List<AccountEcclesiaHistoryDTO> findByStatusAndEcclesiaId(Long ecclesiaUid) {
    return accountEcclesiaHistoryJdbcRepository.findByStatusAndEcclesiaId(ecclesiaUid);
  }

  @Override
  public List<AccountEcclesiaHistoryDetailDTO> findByAccountEcclesiaRequestByEcclesiaUid(
      Long ecclesiaUid) {
    return accountEcclesiaHistoryJdbcRepository.findByAccountEcclesiaRequestByEcclesiaUid(
        ecclesiaUid);
  }
}
