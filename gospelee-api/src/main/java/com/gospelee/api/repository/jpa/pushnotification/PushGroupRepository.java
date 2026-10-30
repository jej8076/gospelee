package com.gospelee.api.repository.jpa.pushnotification;

import com.gospelee.api.entity.PushGroup;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushGroupRepository extends JpaRepository<PushGroup, Long> {

  List<PushGroup> findByEcclesiaUidOrderByNameAsc(Long ecclesiaUid);

  Optional<PushGroup> findByIdAndEcclesiaUid(Long id, Long ecclesiaUid);

  List<PushGroup> findByIdInAndEcclesiaUid(Collection<Long> ids, Long ecclesiaUid);

  boolean existsByEcclesiaUidAndName(Long ecclesiaUid, String name);

  long countByEcclesiaUid(Long ecclesiaUid);
}
