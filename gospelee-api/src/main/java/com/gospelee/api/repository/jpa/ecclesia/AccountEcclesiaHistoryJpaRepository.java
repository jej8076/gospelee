package com.gospelee.api.repository.jpa.ecclesia;

import com.gospelee.api.entity.AccountEcclesiaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountEcclesiaHistoryJpaRepository extends
    JpaRepository<AccountEcclesiaHistory, Long> {

  AccountEcclesiaHistory save(AccountEcclesiaHistory accountEcclesiaHistory);
}
