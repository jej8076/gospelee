package com.gospelee.api.repository.jpa.pushnotification;

import com.gospelee.api.entity.PushNotificationReceivers;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushNotificationReceiversRepository extends
    JpaRepository<PushNotificationReceivers, Long> {

  void deleteByReceiveAccountUid(Long receiveAccountUid);

  Optional<PushNotificationReceivers> findByPushNotificationIdAndReceiveAccountUid(
      Long pushNotificationId, Long receiveAccountUid);

  List<PushNotificationReceivers> findByPushNotificationId(Long pushNotificationId);

  /** 푸시별 상태 건수. 행: [pushNotificationId, status, count] */
  @Query("SELECT r.pushNotificationId, r.status, COUNT(r) FROM PushNotificationReceivers r "
      + "WHERE r.pushNotificationId IN :ids GROUP BY r.pushNotificationId, r.status")
  List<Object[]> countByStatus(@Param("ids") Collection<Long> ids);
}
