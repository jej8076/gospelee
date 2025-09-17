package com.gospelee.api.service;

import com.gospelee.api.dto.common.RedisCacheDTO;
import com.gospelee.api.dto.jwt.JwkSetDTO;
import com.gospelee.api.enums.RedisCacheNames;

public interface RedisCacheService {

  JwkSetDTO getPublicKeySet();

  String put(RedisCacheDTO redisCacheDTO);

  String get(RedisCacheNames redisCacheNames, String key);
}
