package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
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

  @Test
  void trySendEach_splitsIntoChunksOf500_andKeepsOrder() throws Exception {
    FirebaseMessaging messaging = mock(FirebaseMessaging.class);
    // 묶음은 500, 500, 200 순서로 호출된다. 각 묶음의 두 번째 응답만 실패시킨다
    int[] sizes = {500, 500, 200};
    AtomicInteger call = new AtomicInteger();
    when(messaging.sendEachForMulticast(any(MulticastMessage.class))).thenAnswer(inv -> {
      List<SendResponse> responses = new ArrayList<>();
      int size = sizes[call.getAndIncrement()];
      for (int i = 0; i < size; i++) {
        SendResponse r = mock(SendResponse.class);
        when(r.isSuccessful()).thenReturn(i != 1);
        responses.add(r);
      }
      BatchResponse batch = mock(BatchResponse.class);
      when(batch.getResponses()).thenReturn(responses);
      return batch;
    });
    List<String> tokens = IntStream.range(0, 1200).mapToObj(i -> "token-" + i).toList();

    List<Boolean> results = new FirebaseService(messaging).trySendEach(tokens, "t", "b", null);

    assertEquals(1200, results.size());
    verify(messaging, times(3)).sendEachForMulticast(any(MulticastMessage.class));
    assertEquals(3, results.stream().filter(r -> !r).count());
    assertFalse(results.get(1));
    assertFalse(results.get(501));
    assertFalse(results.get(1001));
  }

  @Test
  void trySendEach_marksWholeChunkFailed_whenBatchThrows() throws Exception {
    FirebaseMessaging messaging = mock(FirebaseMessaging.class);
    when(messaging.sendEachForMulticast(any(MulticastMessage.class)))
        .thenThrow(new IllegalStateException("down"));

    List<Boolean> results = new FirebaseService(messaging)
        .trySendEach(List.of("a", "b", "c"), "t", "b", null);

    assertEquals(List.of(false, false, false), results);
  }
}
