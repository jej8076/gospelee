package com.gospelee.api.repository.jpa.announcement;

import com.gospelee.api.entity.Announcement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long>,
    AnnouncementRepositoryCustom {

  @Modifying
  @Query("UPDATE Announcement a SET a.pushNotificationIds = :pushNotificationIds WHERE a.id = :id")
  void updatePushNotificationIdsById(
      @Param("id") Long id,
      @Param("pushNotificationIds") String pushNotificationIds);

  @Modifying
  @Query("UPDATE Announcement a SET a.text = :text WHERE a.id = :id")
  void updateTextById(
      @Param("id") Long id,
      @Param("text") String text);

//  List<Announcement> findByOrganizationTypeAndOrganizationId(String organizationType,
//      String organizationId);

  @Query("SELECT a FROM Announcement a WHERE a.id = :id AND a.organizationType = :organizationType AND a.organizationId = :organizationId")
  Optional<Announcement> findByIdAndOrganizationTypeAndOrganizationId(
      @Param("id") Long id,
      @Param("organizationType") String organizationType,
      @Param("organizationId") Long organizationId);

}
