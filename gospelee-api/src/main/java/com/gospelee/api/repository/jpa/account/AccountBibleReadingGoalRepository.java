package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.AccountBibleReadingGoal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBibleReadingGoalRepository extends JpaRepository<AccountBibleReadingGoal, Long> {

  // 사용자의 현재 진행 중인 목표 조회 (최신 순 1개)
  Optional<AccountBibleReadingGoal> findFirstByAccountUidAndStatusOrderByIdxDesc(Long accountUid, String status);

  // 사용자의 모든 목표 목록 조회
  List<AccountBibleReadingGoal> findAllByAccountUidOrderByIdxDesc(Long accountUid);
}
