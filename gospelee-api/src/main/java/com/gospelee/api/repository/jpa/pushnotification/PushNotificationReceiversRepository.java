package com.gospelee.api.repository.jpa.pushnotification;

import com.gospelee.api.entity.PushNotificationReceivers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationReceiversRepository extends
    JpaRepository<PushNotificationReceivers, Long> {

}
