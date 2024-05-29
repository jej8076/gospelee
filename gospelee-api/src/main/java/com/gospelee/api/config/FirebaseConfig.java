package com.gospelee.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

  @Value("${firebase.admin-sdk.path}")
  private String firebaseSdkPath;

  private FirebaseApp firebaseApp;

  @PostConstruct
  public FirebaseApp initializeFireBase() throws IOException {
    ClassPathResource resource = new ClassPathResource(firebaseSdkPath);
    try (InputStream inputStream = resource.getInputStream()) {
      FirebaseOptions options = new FirebaseOptions.Builder()
          .setCredentials(GoogleCredentials.fromStream(inputStream))
          .build();
      firebaseApp = FirebaseApp.initializeApp(options, "oog");
    } catch (RuntimeException e) {
      e.printStackTrace();
    }

    return firebaseApp;
  }

  @Bean
  public FirebaseAuth initFirebaseAuth() {
    FirebaseAuth instance = FirebaseAuth.getInstance(firebaseApp);
    return instance;
  }

  @Bean
  public FirebaseMessaging initFirebaseMessaging() {
    FirebaseMessaging instance = FirebaseMessaging.getInstance(firebaseApp);
    return instance;
  }
}
