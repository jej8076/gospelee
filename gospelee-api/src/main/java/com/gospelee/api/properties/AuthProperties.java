package com.gospelee.api.properties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "auth")
@Component
@Getter
@Setter
public class AuthProperties {

  private String superId;
  private String superPass;
  private List<String> excludePaths;
  private List<String> allowPendingPaths;
}
