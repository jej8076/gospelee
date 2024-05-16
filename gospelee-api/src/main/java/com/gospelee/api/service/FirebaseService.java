package com.gospelee.api.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.Notification;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class FirebaseService {

  private final String PREFIX_ACCESS_TOKEN = "bearer ";

  @Value("${firebase.admin-sdk.path}")
  private String firebaseSdkPath;

  public void sendMessageTo(final Long receiverId, final Notification notification)
      throws IOException {

    RestTemplate restTemplate = new RestTemplate();
    //알림 요청 받는 사람의 FCM Token이 존재하는지 확인
//    final FcmToken fcmToken = fcmTokenRepository.findByMemberId(receiverId)
//        .orElseThrow(() -> new NotificationException(NOT_FOUND_FCM_TOKEN));

    //메시지 만들기
//    final String message = makeMessage(fcmToken.getToken(), notification);
    final String message = makeMessage("", notification);

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    //OAuth 2.0 사용
    httpHeaders.add(HttpHeaders.AUTHORIZATION, PREFIX_ACCESS_TOKEN + getAccessToken());

    final HttpEntity<String> httpEntity = new HttpEntity<>(message, httpHeaders);

//    final String fcmRequestUrl = PREFIX_FCM_REQUEST_URL + projectId + POSTFIX_FCM_REQUEST_URL;

//    final ResponseEntity<String> exchange = restTemplate.exchange(
//        fcmRequestUrl,
//        HttpMethod.POST,
//        httpEntity,
//        String.class
//    );

  }

  private String makeMessage(final String targetToken, final Notification notification) {

//    final Long senderId = notification.getSenderId();
//    final Member sender = memberRepository.findById(senderId)
//        .orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));
//
//    final Data messageData = new Data(
//        sender.getName(), senderId.toString(),
//        notification.getReceiverId().toString(), notification.getMessage(),
//        sender.getOpenProfileUrl()
//    );
//
//    final Message message = new Message(messageData, targetToken);
//
//    final FcmMessage fcmMessage = new FcmMessage(DEFAULT_VALIDATE_ONLY, message);
//
//    try {
//      return objectMapper.writeValueAsString(fcmMessage);
//    } catch (JsonProcessingException e) {
//      log.error("메세지 보낼 때 JSON 변환 에러", e);
//      throw new NotificationException(CONVERTING_JSON_ERROR);
//    }
    return "";
  }

  private String getAccessToken() throws IOException {
    GoogleCredentials googleCredentials = GoogleCredentials
        .fromStream(new ClassPathResource(firebaseSdkPath).getInputStream())
        .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

    googleCredentials.refreshIfExpired();
    return googleCredentials.getAccessToken().getTokenValue();
  }

}
