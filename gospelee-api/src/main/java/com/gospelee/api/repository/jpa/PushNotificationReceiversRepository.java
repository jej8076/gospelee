package com.gospelee.api.repository.jpa;

import com.gospelee.api.entity.PushNotificationReceivers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationReceiversRepository extends
    JpaRepository<PushNotificationReceivers, Long> {

}
