package com.gospelee.socket.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Component
@Slf4j
public class CustomWebSocketHandler implements WebSocketHandler {

  private final Map<String, Many<String>> channels = new ConcurrentHashMap<>();
  private final ConcurrentMap<WebSocketSession, Boolean> sessionSubscriptions = new ConcurrentHashMap<>();

  @Override
  public Mono<Void> handle(WebSocketSession session) {

    // 클라이언트가 선택한 채널 이름을 추출 (예: 헤더에서 가져오거나, 메시지에서 파싱)
    String channelName = extractChannelName(session); // 구현해야 할 부분

    // 채널에 대한 Sink를 한 번만 생성하고 공유
    Sinks.Many<String> sink = channels.computeIfAbsent(channelName,
        k -> Sinks.many().multicast().onBackpressureBuffer());

    // 서버에서 이미 세션이 구독된 상태라고 판단되면 더 이상 구독하지 않음
    if (sessionSubscriptions.putIfAbsent(session, Boolean.TRUE) == null) {
      session.receive()
          .map(e -> e.getPayloadAsText())
          .map(e -> {
            try {
              // 메시지를 파싱
              JSONObject json = new JSONObject(e);
              String username = json.getString("username");
              if (username.equals("")) {
                username = "익명";
              }
              String message = json.getString("message");
              return username + ": " + message;
            } catch (JSONException ex) {
              ex.printStackTrace();
              return "메시지 처리 중 오류 발생";
            }
          })
          .doOnNext(s -> {
            if (s != null) {
              sink.emitNext(s, Sinks.EmitFailureHandler.FAIL_FAST);
            }
          })
          .doOnError(ex -> log.error("WebSocket receive error", ex))
          .subscribe();
    }

    return session.send(sink.asFlux().map(session::textMessage));
  }

  private String extractChannelName(WebSocketSession session) {
    // 클라이언트가 선택한 채널 이름을 추출하는 로직
    // 예:
    // return session.getHandshakeInfo().getHeaders().getFirst("channel");
    // 또는 메시지에서 파싱하여 채널 이름을 얻음
    return "default"; // 임시로 default 채널로 설정
  }
}
