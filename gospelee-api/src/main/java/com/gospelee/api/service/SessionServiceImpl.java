package com.gospelee.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelee.api.dto.auth.SessionData;
import com.gospelee.api.enums.RedisCacheNames;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SessionServiceImpl implements SessionService {

  private static final String KEY_PREFIX = RedisCacheNames.SESSION.name() + "::";
  private static final int TOKEN_BYTE_LENGTH = 32;

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final SecureRandom secureRandom = new SecureRandom();

  public SessionServiceImpl(RedisTemplate<String, String> redisTemplate,
      ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public String create(SessionData data) {
    String sessionId = generateSessionId();
    String json = serialize(data);
    redisTemplate.opsForValue().set(KEY_PREFIX + sessionId, json, RedisCacheNames.SESSION.ttl());
    return sessionId;
  }

  @Override
  public Optional<SessionData> get(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) {
      return Optional.empty();
    }
    String json = redisTemplate.opsForValue().get(KEY_PREFIX + sessionId);
    if (json == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(deserialize(json));
  }

  @Override
  public void delete(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) {
      return;
    }
    redisTemplate.delete(KEY_PREFIX + sessionId);
  }

  private String generateSessionId() {
    byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String serialize(SessionData data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("세션 직렬화 실패", e);
    }
  }

  private SessionData deserialize(String json) {
    try {
      return objectMapper.readValue(json, SessionData.class);
    } catch (JsonProcessingException e) {
      log.warn("세션 역직렬화 실패: {}", e.getMessage());
      return null;
    }
  }
}
