package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.AccountBibleWrite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountBibleWriteRepository extends JpaRepository<AccountBibleWrite, Long> {

  Optional<List<AccountBibleWrite>> findAllByAccountUid(long accountUid);

  // 사용자별 책별 완료 장 수 조회 (통계용) - 중복 chapter는 DISTINCT로 1회만 카운트
  @Query("SELECT aw.book, COUNT(DISTINCT aw.chapter) FROM AccountBibleWrite aw " +
      "WHERE aw.accountUid = :accountUid GROUP BY aw.book ORDER BY aw.book")
  List<Object[]> getCompletedChaptersByBook(@Param("accountUid") Long accountUid);
}
