package com.gospelee.api.repository.jpa.ministry;

import com.gospelee.api.entity.Ministry;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MinistryRepository extends JpaRepository<Ministry, Long> {

  Optional<Ministry> findByInviteCode(String inviteCode);

  boolean existsByInviteCode(String inviteCode);

  /** 대표 승계·삭제처럼 모임 상태를 바꾸는 작업이 겹치지 않도록 행을 잠그고 조회 */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from Ministry m where m.id = :id")
  Optional<Ministry> findByIdForUpdate(@Param("id") Long id);
}
