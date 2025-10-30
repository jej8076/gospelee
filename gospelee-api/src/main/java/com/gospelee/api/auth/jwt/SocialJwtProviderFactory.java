package com.gospelee.api.auth.jwt;

import com.gospelee.api.enums.SocialLoginPlatform;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SocialJwtProviderFactory {

  private final Map<SocialLoginPlatform, SocialJwtProvider> providers;

  public SocialJwtProviderFactory(List<SocialJwtProvider> providers) {
    this.providers = providers.stream()
        .collect(Collectors.toMap(SocialJwtProvider::getSupportedPlatform, Function.identity()));
  }

  public SocialJwtProvider getProvider(SocialLoginPlatform platform) {
    return providers.get(platform);
  }
}
