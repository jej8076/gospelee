package com.gospelee.api.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.gospelee.api.exception.FirebaseMessagingClientException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirebaseService {

  private final FirebaseMessaging firebaseMessaging;
  private final String apnsEnvironment;

  public FirebaseService(FirebaseMessaging firebaseMessaging) {
    this.firebaseMessaging = firebaseMessaging;
    String activeProfile = System.getProperty("spring.profiles.active", "dev");
    this.apnsEnvironment = "prod".equals(activeProfile) ? "production" : "development";
  }

  /**
   * push 알림 단일 전송
   */
  public String sendNotification(String token, String title, String body)
      throws FirebaseMessagingException {

    return sendNotification(token, title, body, null);
  }

  /**
   * 여러 명에게 보내는 반복 전송용: 만료된 토큰 등으로 실패해도 예외를 던지지 않고 false 반환
   */
  public boolean trySendNotification(String token, String title, String body) {
    return trySendNotification(token, title, body, null);
  }

  /**
   * data에 route를 넣으면 알림을 눌렀을 때 앱이 해당 화면으로 이동한다
   */
  public boolean trySendNotification(String token, String title, String body,
      Map<String, String> data) {
    try {
      sendNotification(token, title, body, data);
      return true;
    } catch (Exception e) {
      log.warn("[FCM] 전송 실패(건너뜀) tokenPrefix={} message={}", maskToken(token),
          e.getMessage());
      return false;
    }
  }

  private static String maskToken(String token) {
    if (token == null || token.length() <= 8) {
      return "***";
    }
    return token.substring(0, 8) + "...";
  }

  public String sendNotification(String token, String title, String body,
      Map<String, String> data) {

    // IOS 설정
    ApnsConfig apnsConfig = ApnsConfig.builder()
        .putHeader("apns-priority", "10")
        .putHeader("apns-environment", apnsEnvironment)
        .setAps(
            Aps.builder()
                .setContentAvailable(false)
                .setMutableContent(false)
                .setSound("default")
                .setBadge(1)
                .setAlert(ApsAlert.builder().build())
                .build()
        ).build();

    // Android 설정
    AndroidConfig androidConfig = AndroidConfig.builder()
        .setPriority(AndroidConfig.Priority.HIGH)
        .setNotification(
            AndroidNotification.builder()
                .setChannelId("podo_notification_channel")
                .setSound("default")
                .setDefaultSound(true)
                .setDefaultVibrateTimings(true)
                .build()
        )
        .build();

    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(body)
        .build();

    Message message = Message.builder()
        .setToken(token)
        .setNotification(notification)
        .putAllData(data == null ? new HashMap<>() : data)
        .setApnsConfig(apnsConfig)
        .setAndroidConfig(androidConfig)
        .build();

    String result = "";
    try {
      result = firebaseMessaging.send(message);
    } catch (FirebaseMessagingException e) {
      throw FirebaseMessagingClientException.from(e);
    }

    return result;
  }

  /**
   * <pre>
   * push 알림 멀티 전송(firebase에서 한 묶음에 500개까지 지원함)
   *
   * @param tokenList
   * @param title
   * @param body
   * @param data
   * @return
   * {
   *  "BatchResponse": {
   *    getSuccessCount: "해당 토큰이 유효하고 Firebase가 메시지를 FCM 전송 큐에 성공적으로 추가했을 경우",
   *    getFailureCount: ["잘못된/만료된 토큰 (registration-token-not-registered)",
   * 	                    "포맷 오류 (invalid-argument)",
   * 	                    "인증 오류 (unauthenticated)",
   * 	                    "메시지 크기 초과, 비허용 필드 등 (invalid-payload)"]
   *    getResponses:
   *  }
   * }
   *
   * </pre>
   */
  public BatchResponse sendMultiNotification(List<String> tokenList, String title, String body,
      Map<String, String> data) {

    String result = "";

    // IOS 설정
    ApnsConfig apnsConfig = ApnsConfig.builder()
        .putHeader("apns-priority", "10")
        .putHeader("apns-environment", apnsEnvironment)
        .setAps(
            Aps.builder()
                .setContentAvailable(false)
                .setMutableContent(false)
                .setSound("default")
                .setBadge(1)
                .setAlert(ApsAlert.builder().build())
                .build()
        ).build();

    // Android 설정
    AndroidConfig androidConfig = AndroidConfig.builder()
        .setPriority(AndroidConfig.Priority.HIGH)
        .setNotification(
            AndroidNotification.builder()
                .setChannelId("podo_notification_channel")
                .setSound("default")
                .setDefaultSound(true)
                .setDefaultVibrateTimings(true)
                .build()
        )
        .build();

    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(body)
        .build();

    MulticastMessage message = MulticastMessage.builder()
        .addAllTokens(tokenList)
        .setNotification(notification)
        .putAllData(data == null ? new HashMap<>() : data)
        .setApnsConfig(apnsConfig)
        .setAndroidConfig(androidConfig)
        .build();

    try {
      return firebaseMessaging.sendEachForMulticast(message);
    } catch (FirebaseMessagingException e) {
      throw new RuntimeException(e);
    }
  }

}
