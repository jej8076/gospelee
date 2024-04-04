package com.gospelee.auth.config;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // ⭐️ CORS 설정
  CorsConfigurationSource corsConfigurationSources() {
    return request -> {
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowedHeaders(Collections.singletonList("*"));
      config.setAllowedMethods(Collections.singletonList("*"));
      config.setAllowedOriginPatterns(
          Collections.singletonList("http://localhost:3000")); // ⭐️ 허용할 origin
      config.setAllowCredentials(true);
      return config;
    };
  }

//  @Bean
//  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
//    return httpSecurity
//        .csrf(AbstractHttpConfigurer::disable)
//        .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configure(httpSecurity)
//        .formLogin(AbstractHttpConfigurer::disable)
//        .httpBasic(AbstractHttpConfigurer::disable)
//        .sessionManagement(sessionManagement -> sessionManagement
//            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//        .build();
  }

}
