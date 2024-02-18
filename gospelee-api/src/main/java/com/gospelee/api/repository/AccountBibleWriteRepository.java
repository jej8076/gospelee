package com.gospelee.api.repository;

import com.gospelee.api.entity.AccountBibleWrite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountBibleWriteRepository extends JpaRepository<AccountBibleWrite, String> {

  Optional<List<AccountBibleWrite>> findAllByAccountUid(long accountUid);

  @Query("SELECT aw FROM AccountBibleWrite aw WHERE aw.accountUid = :accountId AND aw.cate = :cate AND aw.book = :book AND aw.chapter = :chapter")
  Optional<AccountBibleWrite> findByUniqueConstraint(@Param("accountId") Long accountId,
      @Param("cate") Integer cate, @Param("book") Integer book, @Param("chapter") Integer chapter);

  @Modifying
  @Query("UPDATE AccountBibleWrite aw SET aw.count = aw.count + 1 WHERE aw.idx = :idx")
  int increaseCountByIdx(@Param("idx") long idx);
}
