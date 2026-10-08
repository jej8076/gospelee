package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.gospelee.api.entity.Ecclesia;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SlackNotifierTest {

  private static final String WEBHOOK = "https://hooks.slack.test/services/T000/B000/XXXX";

  private Ecclesia church(String name, String telephone) {
    return Ecclesia.builder().uid(5L).name(name).telephone(telephone).status("APL").build();
  }

  private SlackNotifier notifier(RestClient.Builder builder, String webhook, String adminUrl) {
    return new SlackNotifier(builder.build(), webhook, adminUrl, Runnable::run);
  }

  @Test
  void churchRegistered_postsMessageWithContactsToWebhook() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(WEBHOOK))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("포도교회")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("021234567")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("김목사")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("01012345678")))
        .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

    notifier(builder, WEBHOOK, "").notifyChurchRegistered(
        church("포도교회", "021234567"), "김목사", "01012345678");

    server.verify();
  }

  @Test
  void blankWebhook_sendsNothing() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    notifier(builder, "  ", "").notifyChurchRegistered(church("포도교회", null), "김목사", null);

    server.verify();
  }

  @Test
  void webhookFailure_doesNotThrow() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(WEBHOOK)).andRespond(withServerError());

    assertDoesNotThrow(() -> notifier(builder, WEBHOOK, "")
        .notifyChurchRegistered(church("포도교회", "021234567"), "김목사", "01012345678"));

    server.verify();
  }

  @Test
  void message_escapesSlackSpecialCharacters_soNamesCannotMentionOrLink() {
    SlackNotifier notifier = new SlackNotifier(RestClient.builder().build(), WEBHOOK, "", Runnable::run);

    String message = notifier.buildChurchRegisteredMessage(
        church("<!channel> & <https://evil.test|클릭>", "<tel>"), "<@U123>", null,
        LocalDateTime.of(2026, 10, 1, 9, 0));

    assertFalse(message.contains("<!channel>"));
    assertFalse(message.contains("<@U123>"));
    assertTrue(message.contains("&lt;!channel&gt; &amp; &lt;https://evil.test|클릭&gt;"));
  }

  @Test
  void message_showsDeadlineAndOptionalAdminLink() {
    String withLink = new SlackNotifier(RestClient.builder().build(), WEBHOOK,
        "https://admin.example.com/ecclesia", Runnable::run).buildChurchRegisteredMessage(
        church("포도교회", "021234567"), "김목사", "01012345678",
        LocalDateTime.of(2026, 10, 1, 9, 0));
    String withoutLink = new SlackNotifier(RestClient.builder().build(), WEBHOOK, "",
        Runnable::run).buildChurchRegisteredMessage(
        church("포도교회", "021234567"), "김목사", "01012345678",
        LocalDateTime.of(2026, 10, 1, 9, 0));

    assertTrue(withLink.contains("2026-10-15까지"));
    assertTrue(withLink.contains("<https://admin.example.com/ecclesia|관리자 웹 열기>"));
    assertFalse(withoutLink.contains("관리자 웹 열기"));
  }
}
