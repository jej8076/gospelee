package com.gospelee.socket.config;

import com.gospelee.socket.provider.JwtOIDCProvider;
import com.gospelee.socket.service.UserService;
import com.gospelee.socket.websocket.CustomWebSocketHandler;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Sinks;

@Configuration
public class WebsocketConfig {

  private final JwtOIDCProvider jwtOIDCProvider;
  private final UserService userService;

  public WebsocketConfig(JwtOIDCProvider jwtOIDCProvider, UserService userService) {
    this.jwtOIDCProvider = jwtOIDCProvider;
    this.userService = userService;
  }

  @Bean
  public HandlerMapping handlerMapping(WebSocketHandler handler) {
    Map<String, WebSocketHandler> map = new HashMap<>();
    map.put("/socket/1", new CustomWebSocketHandler(jwtOIDCProvider, userService));
    int order = -1; // before annotated controllers

    return new SimpleUrlHandlerMapping(map, order);
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
