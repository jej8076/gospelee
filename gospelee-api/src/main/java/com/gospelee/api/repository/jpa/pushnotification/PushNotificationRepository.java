package com.gospelee.api.repository.jpa.pushnotification;

import com.gospelee.api.entity.PushNotification;
import java.util.List;
import org.springframework.data.domain.Pageable;
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

  /** 교회 성도가 보낸 푸시를 최신순으로 */
  @Query("SELECT p FROM PushNotification p WHERE p.sendAccountUid IN "
      + "(SELECT a.uid FROM Account a WHERE a.ecclesiaUid = :ecclesiaUid) ORDER BY p.id DESC")
  List<PushNotification> findRecentByEcclesia(@Param("ecclesiaUid") Long ecclesiaUid,
      Pageable pageable);

}
