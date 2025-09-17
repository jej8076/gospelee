package com.gospelee.api.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // SnakeCase -> CamelCase
//    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    // JSON 데이터에 DTO 클래스에 정의되지 않은 필드가 있어도 무시하고 직렬화함
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    JavaTimeModule module = new JavaTimeModule();
    module.addSerializer(LocalDateTime.class,
        new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    module.addDeserializer(LocalDateTime.class,
        new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    mapper.registerModule(module);

    return mapper;
  }

  @Bean("snakeCaseObjectMapper")
  public ObjectMapper snakeCaseObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // ✅ 역직렬화만 snake_case → camelCase 필드로 매핑
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    // 기타 설정
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    JavaTimeModule module = new JavaTimeModule();
    module.addSerializer(LocalDateTime.class,
        new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    module.addDeserializer(LocalDateTime.class,
        new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    mapper.registerModule(module);

    return mapper;
  }
}
