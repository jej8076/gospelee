package com.gospelee.api.service;

import com.gospelee.api.dto.jwt.JwkSet;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

  JwkSet getPublicKeySet();
}
