package com.gospelee.socket.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gospelee.socket.dto.jwt.JwtPayload;
import com.gospelee.socket.provider.JwtOIDCProvider;
import com.gospelee.socket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/user")
public class UserController {

  private final UserService userService;
  private final JwtOIDCProvider jwtOIDCProvider;

  @PostMapping
  public ResponseEntity<Object> getUser(@RequestHeader("token") String token)
      throws JsonProcessingException {
    JwtPayload payload = jwtOIDCProvider.getOIDCPayload(token);
    return new ResponseEntity<>(userService.findUser(payload.getEmail()), HttpStatus.OK);
  }
}
