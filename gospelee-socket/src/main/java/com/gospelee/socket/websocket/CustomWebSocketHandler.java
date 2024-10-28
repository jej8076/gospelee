package com.gospelee.socket.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gospelee.socket.dto.PlayerAction;
import com.gospelee.socket.dto.jwt.JwtPayload;
import com.gospelee.socket.provider.JwtOIDCProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;
import util.JsonUtils;

@Component
@Slf4j
public class CustomWebSocketHandler implements WebSocketHandler {

  private final JwtOIDCProvider jwtOIDCProvider;
  private final Map<String, Many<String>> channels = new ConcurrentHashMap<>();
  private final ConcurrentMap<WebSocketSession, Boolean> sessionSubscriptions = new ConcurrentHashMap<>();

  public CustomWebSocketHandler(JwtOIDCProvider jwtOIDCProvider) {
    this.jwtOIDCProvider = jwtOIDCProvider;
  }

  @Override
  public Mono<Void> handle(WebSocketSession session) {

    String token = session.getHandshakeInfo().getHeaders().getFirst("Authorization");
    JwtPayload payload;

    // test
    if (!"Bearer your_token".equals(token)) {
      try {
        payload = jwtOIDCProvider.getOIDCPayload(token);
      } catch (JsonProcessingException e) {
        log.error(e.getMessage());
        return session.close();
      }

      if (payload == null) {
        return session.close();
      }
    }

    // 클라이언트가 선택한 채널 이름을 추출 (예: 헤더에서 가져오거나, 메시지에서 파싱)
    String channelName = extractChannelName(session);

    // 채널에 대한 Sink를 한 번만 생성하고 공유
    Sinks.Many<String> sink = channels.computeIfAbsent(channelName,
        k -> Sinks.many().multicast().onBackpressureBuffer());

    // 서버에서 이미 세션이 구독된 상태라고 판단되면 더 이상 구독하지 않음
    if (sessionSubscriptions.putIfAbsent(session, Boolean.TRUE) == null) {
      session
          .receive()
          .map(WebSocketMessage::getPayloadAsText)
          .mapNotNull(e -> {
            try {
              PlayerAction action = JsonUtils.toObjectFromString(e, PlayerAction.class);
              return JsonUtils.toStringFromObject(action);
            } catch (RuntimeException ex) {
              throw new RuntimeException(ex);
            }
          })
          .doOnNext(s -> {
            if (s != null) {
              sink.emitNext(s, EmitFailureHandler.FAIL_FAST);
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
