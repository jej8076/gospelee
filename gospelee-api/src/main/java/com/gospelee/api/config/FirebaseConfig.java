package com.gospelee.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Configuration
public class FirebaseConfig {

  private static final String CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
  private static final String FIREBASE_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

  @Value("${firebase.admin-sdk.path}")
  private String firebaseSdkPath;

  private FirebaseApp firebaseApp;

  @PostConstruct
  public FirebaseApp initializeFireBase() throws IOException {
    Resource resource = new FileSystemResource(firebaseSdkPath);
    try (InputStream inputStream = resource.getInputStream()) {

      GoogleCredentials credentials = GoogleCredentials
          .fromStream(inputStream)
          .createScoped(List.of(CLOUD_PLATFORM_SCOPE, FIREBASE_MESSAGING_SCOPE));

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(credentials)
          .build();

      firebaseApp = FirebaseApp.initializeApp(options, "oog-dev");
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
