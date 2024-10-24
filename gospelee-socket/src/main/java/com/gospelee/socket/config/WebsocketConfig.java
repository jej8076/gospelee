package com.gospelee.socket.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Sinks;

@Configuration
public class WebsocketConfig {

  @Bean
  public SimpleUrlHandlerMapping handlerMapping(WebSocketHandler handler) {
    return new SimpleUrlHandlerMapping(Map.of("/ws-chat", handler), 1);
  }

  @Bean
  public WebSocketHandlerAdapter webSocketHandlerAdapter() {
    return new WebSocketHandlerAdapter();
  }

  @Bean
  public Sinks.Many<String> sink() {
    return Sinks.many().multicast().directBestEffort();
  }

}
