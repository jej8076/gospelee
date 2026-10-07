package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.Test;

class FirebaseServiceTrySendTest {

  @Test
  void trySend_returnsTrueOnSuccess() throws Exception {
    FirebaseMessaging messaging = mock(FirebaseMessaging.class);
    when(messaging.send(any(Message.class))).thenReturn("message-id");

    assertTrue(new FirebaseService(messaging).trySendNotification("token-12345678", "t", "b"));
  }

  @Test
  void trySend_returnsFalseInsteadOfThrowing_whenSendFails() throws Exception {
    FirebaseMessaging messaging = mock(FirebaseMessaging.class);
    when(messaging.send(any(Message.class))).thenThrow(new IllegalStateException("not registered"));

    assertFalse(new FirebaseService(messaging).trySendNotification("token-12345678", "t", "b"));
  }
}
