package com.gospelee.api.dto.common;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class DataResponseDTO<T> extends ResponseDTO {

  private final T data; // 결과 데이터

  public DataResponseDTO(String code, String description, T data) {
    super(code, description);
    this.data = data;
  }

  public static <T> DataResponseDTO<T> of(String code, String description, T data) {
    return new DataResponseDTO<>(code, description, data);
  }
}
