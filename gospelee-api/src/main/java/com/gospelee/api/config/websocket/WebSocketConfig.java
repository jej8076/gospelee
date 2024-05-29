package com.gospelee.api.config.websocket;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(new MyWebSocketHandler(), "/ws")
        .setAllowedOrigins("*")
        .addInterceptors(new HttpSessionHandshakeInterceptor() {
          @Override
          public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
              WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            if (request instanceof ServletServerHttpRequest) {
              ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
              HttpServletRequest httpRequest = servletRequest.getServletRequest();
              httpRequest.setAttribute("Upgrade", "websocket");
              httpRequest.setAttribute("Connection", "Upgrade");
//              httpRequest.setAttribute("Host", httpRequest.getServerName());
              System.out.println(httpRequest.getServerName());
            }
            return super.beforeHandshake(request, response, wsHandler, attributes);
          }
        });
  }
}