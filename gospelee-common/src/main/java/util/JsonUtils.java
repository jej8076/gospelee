package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  // JSON 문자열을 객체 리스트로 변환하는 제네릭 메소드
  public static <T> List<T> toObjectListFromString(String jsonString, Class<T> clazz) {
    try {

      if (!(jsonString.startsWith("[") && jsonString.endsWith("]"))) {
        log.error("Invalid JSON string: {}", jsonString);
        return new ArrayList<>();
      }

      return objectMapper.readValue(jsonString, new TypeReference<List<T>>() {
      });
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  // JSON 문자열을 객체로 변환하는 제네릭 메소드
  public static <T> T toObjectFromString(String jsonString, Class<T> clazz) {
    try {
      return objectMapper.readValue(jsonString, clazz);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }

  }

  // 객체를 JSON 문자열로 변환하는 제네릭 메소드
  public static <T> String toStringFromObject(T object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static List<String> toStringListFromString(String jsonString) {
    try {
      if (jsonString == null || jsonString.isEmpty()) {
        throw new IllegalArgumentException("Input string cannot be null or empty");
      }

      return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {
      });
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static String toStringFromStringList(List<String> list) {
    try {
      if (list == null) {
        throw new IllegalArgumentException("Input list cannot be null");
      }

      return objectMapper.writeValueAsString(list);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String toStringFromStringList(String[] list) {
    try {
      if (list == null) {
        throw new IllegalArgumentException("Input list cannot be null");
      }

      return objectMapper.writeValueAsString(list);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static <T> String toStringFromObjectList(List<T> objectList) {
    try {
      if (objectList == null) {
        throw new IllegalArgumentException("Input list cannot be null");
      }
      return objectMapper.writeValueAsString(objectList);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static <T> T getOneByDTOList(int idx, List<?> objectList, Class<T> clazz) {
    if (objectList == null) {
      throw new IllegalArgumentException("Input list cannot be null");
    }
    if (idx < 0 || idx >= objectList.size()) {
      throw new IndexOutOfBoundsException("Index out of bounds");
    }

    Object obj = objectList.get(idx);
    return objectMapper.convertValue(obj, clazz);
  }

}
