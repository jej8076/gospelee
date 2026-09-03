package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.AccountBibleRead;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountBibleReadRepository extends JpaRepository<AccountBibleRead, Long> {

  // 특정 책에 대해 사용자가 읽은 기록 전체 조회 (목표 무관)
  List<AccountBibleRead> findAllByAccountUidAndBook(Long accountUid, int book);

  // 특정 목표, 책에 대해 사용자가 읽은 기록 전체 조회
  List<AccountBibleRead> findAllByAccountUidAndGoalIdxAndBook(Long accountUid, Long goalIdx, int book);

  // 특정 책, 장에 대한 기록 조회 (목표 무관)
  Optional<AccountBibleRead> findFirstByAccountUidAndBookAndChapter(Long accountUid, int book, int chapter);

  // 특정 목표, 책, 장에 대한 기록 조회
  Optional<AccountBibleRead> findFirstByAccountUidAndGoalIdxAndBookAndChapter(Long accountUid, Long goalIdx, int book, int chapter);

  // 특정 책, 장 다중 삭제 (읽음 해제 - 목표 무관)
  @Modifying
  @Query("DELETE FROM AccountBibleRead ar WHERE ar.accountUid = :accountUid AND ar.book = :book AND ar.chapter IN :chapters")
  void deleteByAccountUidAndBookAndChapterIn(@Param("accountUid") Long accountUid,
      @Param("book") int book, @Param("chapters") Collection<Integer> chapters);

  // 특정 목표 내 특정 책, 장 다중 삭제 (읽음 해제)
  @Modifying
  @Query("DELETE FROM AccountBibleRead ar WHERE ar.accountUid = :accountUid AND ar.goalIdx = :goalIdx AND ar.book = :book AND ar.chapter IN :chapters")
  void deleteByAccountUidAndGoalIdxAndBookAndChapterIn(@Param("accountUid") Long accountUid,
      @Param("goalIdx") Long goalIdx, @Param("book") int book, @Param("chapters") Collection<Integer> chapters);

  // 특정 목표에 속한 모든 읽음 기록 일괄 삭제 (목표 종료/삭제 시)
  @Modifying
  @Query("DELETE FROM AccountBibleRead ar WHERE ar.accountUid = :accountUid AND ar.goalIdx = :goalIdx")
  void deleteByAccountUidAndGoalIdx(@Param("accountUid") Long accountUid, @Param("goalIdx") Long goalIdx);

  // 목표별 책별 완료 장 수 조회 (중복 chapter는 DISTINCT로 1회 카운트)
  @Query("SELECT ar.book, COUNT(DISTINCT ar.chapter) FROM AccountBibleRead ar " +
      "WHERE ar.accountUid = :accountUid AND ar.goalIdx = :goalIdx GROUP BY ar.book ORDER BY ar.book")
  List<Object[]> getCompletedChaptersByGoal(@Param("accountUid") Long accountUid, @Param("goalIdx") Long goalIdx);

  // 사용자별 책별 완료 장 수 조회 (중복 chapter는 DISTINCT로 1회 카운트)
  @Query("SELECT ar.book, COUNT(DISTINCT ar.chapter) FROM AccountBibleRead ar " +
      "WHERE ar.accountUid = :accountUid GROUP BY ar.book ORDER BY ar.book")
  List<Object[]> getCompletedChaptersByBook(@Param("accountUid") Long accountUid);

  // 전체 완료된 고유 장(DISTINCT book, chapter) 총 개수
  @Query("SELECT COUNT(DISTINCT CONCAT(ar.book, '-', ar.chapter)) FROM AccountBibleRead ar WHERE ar.accountUid = :accountUid")
  long countDistinctReadChapters(@Param("accountUid") Long accountUid);

  // 캘린더용: 기간 내 날짜별 읽은 장 수 조회
  @Query("SELECT ar.readDate, COUNT(ar.idx) FROM AccountBibleRead ar " +
      "WHERE ar.accountUid = :accountUid AND ar.readDate BETWEEN :startDate AND :endDate " +
      "GROUP BY ar.readDate ORDER BY ar.readDate")
  List<Object[]> countReadByDateBetween(@Param("accountUid") Long accountUid,
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  // 캘린더용: 특정 날짜의 상세 읽기 목록 조회
  List<AccountBibleRead> findAllByAccountUidAndReadDateOrderByBookAscChapterAsc(Long accountUid, LocalDate readDate);
}
