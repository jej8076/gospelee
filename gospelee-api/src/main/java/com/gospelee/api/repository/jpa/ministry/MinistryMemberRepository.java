package com.gospelee.api.repository.jpa.ministry;

import com.gospelee.api.entity.MinistryMember;
import com.gospelee.api.enums.MinistryMemberStatus;
import com.gospelee.api.enums.MinistryRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinistryMemberRepository extends JpaRepository<MinistryMember, Long> {

  Optional<MinistryMember> findByMinistryIdAndAccountUid(Long ministryId, Long accountUid);

  List<MinistryMember> findByAccountUid(Long accountUid);

  List<MinistryMember> findByMinistryId(Long ministryId);

  List<MinistryMember> findByMinistryIdAndStatus(Long ministryId, MinistryMemberStatus status);

  long countByMinistryIdAndStatus(Long ministryId, MinistryMemberStatus status);

  long countByAccountUidAndRole(Long accountUid, MinistryRole role);

  long countByAccountUidAndStatusIn(Long accountUid, Collection<MinistryMemberStatus> statuses);

  void deleteByMinistryId(Long ministryId);
}
