package com.gospelee.api.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.ApnsFcmOptions;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

  private final FirebaseMessaging firebaseMessaging;

  public FirebaseService(FirebaseMessaging firebaseMessaging) {
    this.firebaseMessaging = firebaseMessaging;
  }

  /**
   * push 알림 단일 전송
   *
   * @param pushToken
   * @param title
   * @param content
   * @return
   * @throws FirebaseMessagingException
   */
  public String sendNotification(String pushToken, String title, String content)
      throws FirebaseMessagingException {

    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(content)
        .build();

    // apple APNs setting
    Aps aps = Aps.builder()
        .setSound("default")
        .setContentAvailable(true)
        .build();
    ApnsConfig apnsConfig = ApnsConfig.builder()
        .setAps(aps)
        .build();

    Message message = Message.builder()
        .setNotification(notification)
        .setApnsConfig(apnsConfig)
        .setToken(pushToken)
        .build();

    return firebaseMessaging.send(message);
  }

  public String sendNotification(String token, String title, String body,
      Map<String, String> data) {

    // IOS 설정
    ApnsConfig apnsConfig = ApnsConfig.builder()
        .putHeader("apns-priority", "10")
        .setAps(
            Aps.builder()
                .setContentAvailable(false)
                .setMutableContent(false)
                .setSound("default")
                .setBadge(1)
                .build()
        ).build();

    // Android 설정
    AndroidConfig androidConfig = AndroidConfig.builder()
        .setPriority(AndroidConfig.Priority.HIGH)
        .build();

    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(body)
        .build();

    Message message = Message.builder()
        .setToken(token)
        .setNotification(notification)
        .putAllData(data)
        .setApnsConfig(apnsConfig)
        .setAndroidConfig(androidConfig)
        .build();
    
    String result = "";
    try {
      result = firebaseMessaging.send(message);
    } catch (FirebaseMessagingException e) {
      e.getMessage();
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

    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(body)
        .build();

    MulticastMessage message = MulticastMessage.builder()
        .addAllTokens(tokenList)
        .setNotification(notification)
        .putAllData(data)
        .build();

    try {
      return firebaseMessaging.sendMulticast(message);
    } catch (FirebaseMessagingException e) {
      throw new RuntimeException(e);
    }
  }
}
