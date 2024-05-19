package com.gospelee.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.IOException;
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
    FileInputStream fis = new FileInputStream(resource.getFile());
    FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(fis))
        .build();
    firebaseApp = FirebaseApp.initializeApp(options, "oog");
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