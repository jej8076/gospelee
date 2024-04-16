package com.gospelee.api.standard;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CommonResponse {

  public static ResponseEntity<Object> response(Object obj, HttpStatus status) {
    Map<String, Object> response = new HashMap<>();
    response.put("status", HttpStatus.OK);
    response.put("body", obj);
    return new ResponseEntity<>(response, status);
  }

}
