package com.gospelee.api.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum Bearer {
  BEARER_SPACE("Bearer "),
  ;

  @Getter
  final private String value;

  Bearer(String value) {
    this.value = value;
  }

}
