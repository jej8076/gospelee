package com.gospelee.socket.provider;

import java.lang.reflect.Method;
import org.springframework.cache.interceptor.KeyGenerator;

public class CustomKeyGenerator implements KeyGenerator {

  private static final String KEY_FORMAT = "kakao";

  @Override
  public Object generate(Object target, Method method, Object... params) {
    return KEY_FORMAT;
  }
}
