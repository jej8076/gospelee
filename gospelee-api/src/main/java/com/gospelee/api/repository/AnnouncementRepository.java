package com.gospelee.api.repository;

import com.gospelee.api.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

  @Modifying
  @Query("UPDATE Announcement a SET a.pushNotificationIds = :pushNotificationIds WHERE a.id = :id")
  void updatePushNotificationIdsById(
      @Param("id") Long id,
      @Param("pushNotificationIds") String pushNotificationIds);
}
