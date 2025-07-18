package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.AccountEcclesiaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountEcclesiaHistoryRepository extends
    JpaRepository<AccountEcclesiaHistory, Long> {

}
