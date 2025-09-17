package com.gospelee.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

/**
 * snake case의 DTO를 직렬화, 역직렬화 하기 위한 Utils
 */
@Component
public class SnakeCaseJsonUtils {

  private final ObjectMapper objectMapper;

  public SnakeCaseJsonUtils(@Qualifier("snakeCaseObjectMapper") ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> T deserialization(String json, Class<T> tClass) {
    if (json != null) {
      try {
        return objectMapper.readValue(json, tClass);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(tClass.getSimpleName() + " 역직렬화 실패", e);
      }
    }
    return null;
  }

  public MappingJackson2HttpMessageConverter converter() {
    return new MappingJackson2HttpMessageConverter(objectMapper);
  }

}
