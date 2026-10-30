package com.gospelee.api.repository.jpa.pushnotification;

import com.gospelee.api.entity.PushGroupMember;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushGroupMemberRepository extends JpaRepository<PushGroupMember, Long> {

  List<PushGroupMember> findByGroupId(Long groupId);

  List<PushGroupMember> findByGroupIdIn(Collection<Long> groupIds);

  void deleteByGroupId(Long groupId);
}
