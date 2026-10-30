package com.gospelee.api.repository.jpa.bulletin;

import com.gospelee.api.entity.Bulletin;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulletinRepository extends JpaRepository<Bulletin, Long> {

  /** 교회의 주보를 예배 날짜 최신순으로 조회 (openYn 조건 포함) */
  List<Bulletin> findByEcclesiaUidAndOpenYnOrderByWorshipDateDesc(Long ecclesiaUid,
      String openYn, Pageable pageable);

  /** 교회의 주보를 예배 날짜 최신순으로 조회 (공개 여부 무관, 관리자용) */
  List<Bulletin> findByEcclesiaUidOrderByWorshipDateDesc(Long ecclesiaUid, Pageable pageable);

  Optional<Bulletin> findByIdAndEcclesiaUid(Long id, Long ecclesiaUid);

  boolean existsByEcclesiaUidAndWorshipDate(Long ecclesiaUid, LocalDate worshipDate);

  Optional<Bulletin> findByEcclesiaUidAndWorshipDate(Long ecclesiaUid, LocalDate worshipDate);
}
