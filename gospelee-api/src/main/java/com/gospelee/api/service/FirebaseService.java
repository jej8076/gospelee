package com.gospelee.api.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

  private final FirebaseMessaging firebaseMessaging;

  public FirebaseService(FirebaseMessaging firebaseMessaging) {
    this.firebaseMessaging = firebaseMessaging;
  }

  public String sendNotification(String pushToken, String title, String content)
      throws FirebaseMessagingException {

    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(content)
        .build();

    Message message = Message.builder()
        .setNotification(notification)
        .setToken(pushToken)
        .build();

    return firebaseMessaging.send(message);
  }

}