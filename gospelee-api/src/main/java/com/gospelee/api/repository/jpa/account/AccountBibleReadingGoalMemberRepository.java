package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.AccountBibleReadingGoalMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBibleReadingGoalMemberRepository extends JpaRepository<AccountBibleReadingGoalMember, Long> {

  Optional<AccountBibleReadingGoalMember> findByGoalIdxAndAccountUid(Long goalIdx, Long accountUid);

  List<AccountBibleReadingGoalMember> findAllByGoalIdxAndStatusOrderByJoinedAtAsc(Long goalIdx, String status);

  List<AccountBibleReadingGoalMember> findAllByAccountUidAndStatusOrderByJoinedAtDesc(Long accountUid, String status);

  int countByGoalIdxAndStatus(Long goalIdx, String status);

  boolean existsByGoalIdxAndAccountUidAndStatus(Long goalIdx, Long accountUid, String status);

  Optional<AccountBibleReadingGoalMember> findFirstByGoalIdxAndStatusAndRoleOrderByJoinedAtAsc(Long goalIdx, String status, String role);
}
