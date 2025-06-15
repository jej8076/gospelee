package com.gospelee.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Configuration
public class FirebaseConfig {

  private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

  private static final String CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
  private static final String FIREBASE_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
  private static final String APP_NAME = "oog-dev"; // Firebase 앱의 고유 이름

  // application.properties 또는 application.yml 파일에 정의된 firebase.admin-sdk.path 속성 값을 주입받습니다.
  @Value("${firebase.admin-sdk.path}")
  private String firebaseSdkPath;

  /**
   * FirebaseApp을 초기화하거나 이미 초기화된 경우 기존 인스턴스를 반환하는 Bean 메서드입니다. 이 메서드는 Spring 컨테이너에 의해 자동으로 호출되어
   * FirebaseApp 인스턴스를 생성하고 관리합니다. 이 메서드는 다른 Firebase 서비스 Bean들이 의존하는 FirebaseApp Bean을 제공합니다.
   *
   * @return 초기화된 FirebaseApp 인스턴스
   * @throws IOException 서비스 계정 파일을 읽을 수 없을 때 발생 (파일을 찾거나 읽을 수 없을 때)
   */
  @Bean
  public FirebaseApp initializeFireBase() throws IOException {
    logger.info("Attempting to initialize FirebaseApp '{}'...", APP_NAME);

    // 이미 APP_NAME을 가진 FirebaseApp이 초기화되어 있는지 확인합니다.
    Optional<FirebaseApp> existingApp = FirebaseApp.getApps().stream()
        .filter(app -> app.getName().equals(APP_NAME))
        .findFirst();

    if (existingApp.isPresent()) {
      // 이미 초기화된 앱이 있다면, 그 앱의 인스턴스를 반환합니다.
      logger.info("FirebaseApp '{}' is already initialized. Returning existing instance.",
          APP_NAME);
      return existingApp.get();
    } else {
      // 초기화되어 있지 않다면, 새로운 FirebaseApp 인스턴스를 초기화합니다.
      logger.info("FirebaseApp '{}' not found. Proceeding with new initialization.", APP_NAME);
      logger.info("Firebase SDK Path configured: {}", firebaseSdkPath); // 경로 로깅

      Resource resource = new FileSystemResource(firebaseSdkPath);

      // 파일 존재 및 읽기 권한 사전 확인 로직 (디버깅에 유용)
      if (!resource.exists()) {
        logger.error("Firebase service account file NOT FOUND at path: {}", firebaseSdkPath);
        throw new IOException("Firebase service account file not found at: " + firebaseSdkPath);
      }
      if (!resource.isReadable()) {
        logger.error("Firebase service account file NOT READABLE at path: {}", firebaseSdkPath);
        throw new IOException(
            "Firebase service account file is not readable at: " + firebaseSdkPath);
      }

      try (InputStream inputStream = resource.getInputStream()) {
        // 서비스 계정 키 파일을 사용하여 GoogleCredentials를 생성하고 필요한 스코프를 추가합니다.
        GoogleCredentials credentials = GoogleCredentials
            .fromStream(inputStream)
            .createScoped(List.of(CLOUD_PLATFORM_SCOPE, FIREBASE_MESSAGING_SCOPE));

        // FirebaseOptions를 빌드하여 자격 증명을 설정합니다.
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build();

        // FirebaseApp을 지정된 이름으로 초기화하고 반환합니다.
        // APP_NAME을 사용하여 앱의 고유 이름을 설정합니다.
        FirebaseApp newApp = FirebaseApp.initializeApp(options, APP_NAME);
        logger.info("FirebaseApp '{}' initialized successfully.", APP_NAME);
        return newApp;
      } catch (IOException e) {
        logger.error("Failed to initialize FirebaseApp '{}' due to IOException: {}", APP_NAME,
            e.getMessage(), e);
        // IO 예외는 Spring 컨텍스트 초기화 실패의 원인이 되므로 다시 던집니다.
        throw e;
      } catch (RuntimeException e) {
        logger.error("Failed to initialize FirebaseApp '{}' due to RuntimeException: {}", APP_NAME,
            e.getMessage(), e);
        e.printStackTrace();
        // 다른 런타임 예외도 Spring 컨텍스트 초기화 실패의 원인이 되므로 다시 던집니다.
        throw e;
      }
    }
  }

  /**
   * FirebaseAuth 인스턴스를 제공하는 Bean 메서드입니다. 이 메서드는 Spring이 initializeFireBase() 메서드를 통해 생성된
   * FirebaseApp 인스턴스를 주입받아 사용합니다.
   *
   * @param firebaseApp initializeFireBase() Bean에서 Spring이 주입한 FirebaseApp 인스턴스
   * @return FirebaseAuth 인스턴스
   */
  @Bean
  public FirebaseAuth initFirebaseAuth(FirebaseApp firebaseApp) {
    // 이제 firebaseApp 파라미터는 Spring에 의해 성공적으로 초기화된 FirebaseApp 인스턴스가 주입됨을 보장합니다.
    logger.info("Initializing FirebaseAuth using FirebaseApp '{}'...", firebaseApp.getName());
    return FirebaseAuth.getInstance(firebaseApp);
  }

  /**
   * FirebaseMessaging 인스턴스를 제공하는 Bean 메서드입니다. 이 메서드는 Spring이 initializeFireBase() 메서드를 통해 생성된
   * FirebaseApp 인스턴스를 주입받아 사용합니다.
   *
   * @param firebaseApp initializeFireBase() Bean에서 Spring이 주입한 FirebaseApp 인스턴스
   * @return FirebaseMessaging 인스턴스
   */
  @Bean
  public FirebaseMessaging initFirebaseMessaging(FirebaseApp firebaseApp) {
    // 이제 firebaseApp 파라미터는 Spring에 의해 성공적으로 초기화된 FirebaseApp 인스턴스가 주입됨을 보장합니다.
    logger.info("Initializing FirebaseMessaging using FirebaseApp '{}'...", firebaseApp.getName());
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}
