package com.gospelee.api.service;

import com.gospelee.api.entity.Ecclesia;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClient;

/**
 * 운영자용 Slack 알림 (Incoming Webhook).
 * 웹훅 주소(slack.webhook-url)가 비어 있으면 아무것도 보내지 않으며,
 * 전송이 실패해도 예외를 던지지 않고 로그만 남겨 본 작업(교회 등록 등)에 영향을 주지 않는다.
 */
@Slf4j
@Component
public class SlackNotifier {

  private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Duration TIMEOUT = Duration.ofSeconds(3);

  private final RestClient restClient;
  private final String webhookUrl;
  private final String adminUrl;
  private final Executor executor;

  @Autowired
  public SlackNotifier(RestClient.Builder builder,
      @Value("${slack.webhook-url:}") String webhookUrl,
      @Value("${slack.admin-url:}") String adminUrl) {
    this(builder.requestFactory(timeoutRequestFactory()).build(), webhookUrl, adminUrl,
        ForkJoinPool.commonPool());
  }

  SlackNotifier(RestClient restClient, String webhookUrl, String adminUrl, Executor executor) {
    this.restClient = restClient;
    this.webhookUrl = webhookUrl == null ? "" : webhookUrl.trim();
    this.adminUrl = adminUrl == null ? "" : adminUrl.trim();
    this.executor = executor;
  }

  private static JdkClientHttpRequestFactory timeoutRequestFactory() {
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
        HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
    factory.setReadTimeout(TIMEOUT);
    return factory;
  }

  /**
   * 새 교회가 등록되었음을 알린다. 운영자가 전화로 확인할 수 있도록 연락처를 포함한다.
   * 등록 트랜잭션이 커밋된 뒤에 비동기로 전송한다(롤백되면 보내지 않는다).
   */
  public void notifyChurchRegistered(Ecclesia ecclesia, String applicantName,
      String applicantPhone) {
    if (webhookUrl.isEmpty()) {
      return;
    }

    String message = buildChurchRegisteredMessage(ecclesia, applicantName, applicantPhone,
        LocalDateTime.now());
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          sendAsync(message);
        }
      });
    } else {
      sendAsync(message);
    }
  }

  String buildChurchRegisteredMessage(Ecclesia ecclesia, String applicantName,
      String applicantPhone, LocalDateTime now) {
    StringBuilder sb = new StringBuilder();
    sb.append(":church: *새 교회 등록* — ").append(escape(ecclesia.getName())).append('\n');
    sb.append("• 교회 전화: ").append(orDash(ecclesia.getTelephone())).append('\n');
    sb.append("• 신청자: ").append(orDash(applicantName))
        .append(" (").append(orDash(applicantPhone)).append(")\n");
    sb.append("• 신청 시각: ").append(now.format(DATE_TIME)).append('\n');
    sb.append("14일 안에(").append(now.plusDays(Ecclesia.VERIFICATION_DEADLINE_DAYS).format(DATE))
        .append("까지) 전화로 확인한 뒤 관리자 웹에서 *검증 완료*를 눌러주세요.");
    if (!adminUrl.isEmpty()) {
      sb.append("\n<").append(adminUrl).append("|관리자 웹 열기>");
    }
    return sb.toString();
  }

  private void sendAsync(String text) {
    try {
      executor.execute(() -> send(text));
    } catch (Exception e) {
      log.warn("[SLACK] 전송 작업 등록 실패", e);
    }
  }

  void send(String text) {
    try {
      restClient.post()
          .uri(webhookUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("text", text))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      // 웹훅 주소는 비밀 값이므로 오류 메시지에 주소가 남지 않도록 클래스명만 기록
      log.warn("[SLACK] 전송 실패 error={}", e.getClass().getSimpleName());
    }
  }

  // 사용자 입력(교회 이름 등)이 멘션/링크로 해석되지 않도록 Slack 특수문자를 이스케이프
  static String escape(String value) {
    if (ObjectUtils.isEmpty(value)) {
      return "-";
    }
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static String orDash(String value) {
    return escape(value);
  }
}
