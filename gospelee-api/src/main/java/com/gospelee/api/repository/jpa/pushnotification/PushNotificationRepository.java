package com.gospelee.api.repository.jpa.pushnotification;

import com.gospelee.api.entity.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {

  @Modifying
  @Query("UPDATE PushNotification p SET p.totalCount = :totalCount WHERE p.id = :id")
  void updateTotalcountById(
      @Param("id") Long id,
      @Param("totalCount") Integer totalCount);

}
