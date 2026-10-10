package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.AccountBlock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBlockRepository extends JpaRepository<AccountBlock, Long> {

  List<AccountBlock> findByBlockerUid(Long blockerUid);

  Optional<AccountBlock> findByBlockerUidAndBlockedUid(Long blockerUid, Long blockedUid);

  boolean existsByBlockerUidAndBlockedUid(Long blockerUid, Long blockedUid);
}
