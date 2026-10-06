package com.gospelee.api.repository.jpa.ecclesia;

import com.gospelee.api.entity.AccountEcclesiaHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountEcclesiaHistoryJpaRepository extends
    JpaRepository<AccountEcclesiaHistory, Long> {

  AccountEcclesiaHistory save(AccountEcclesiaHistory accountEcclesiaHistory);

  // 계정의 가장 최근 교회 이력 (id가 증가하므로 id 기준으로 최신 판단)
  Optional<AccountEcclesiaHistory> findFirstByAccountUidOrderByIdDesc(long accountUid);
}
