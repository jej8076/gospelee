package com.gospelee.api.service.push;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.pushdirect.PushDirectRequestDTO;
import com.gospelee.api.dto.pushdirect.PushDirectResponseDTO;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.service.push.PushDispatchService.PushRequest;
import com.gospelee.api.service.push.PushDispatchService.SendResult;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.security.access.AccessDeniedException;

class PushDirectServiceTest {

  private PushDispatchService dispatchService;
  private AnnouncementRepository announcementRepository;
  private PushDirectService service;
  private MockedStatic<AuthenticatedUserUtils> auth;

  @BeforeEach
  void setUp() {
    dispatchService = mock(PushDispatchService.class);
    announcementRepository = mock(AnnouncementRepository.class);
    service = new PushDirectService(dispatchService, announcementRepository);
    auth = mockStatic(AuthenticatedUserUtils.class);
    loginAs(RoleType.PASTOR);
    when(dispatchService.dispatch(any())).thenReturn(new SendResult(55L, 10, 8, 1, 1));
  }

  @AfterEach
  void tearDown() {
    auth.close();
  }

  private void loginAs(RoleType role) {
    auth.when(AuthenticatedUserUtils::getAuthenticatedUserOrElseThrow).thenReturn(
        AccountAuthDTO.builder().uid(1L).role(role).ecclesiaUid(10L).build());
  }

  private PushDirectRequestDTO request(String title, String message, List<Long> groupIds) {
    return PushDirectRequestDTO.builder().title(title).message(message).groupIds(groupIds).build();
  }

  private PushDirectRequestDTO requestWithRoute(String route) {
    return PushDirectRequestDTO.builder().title("t").message("m").route(route).build();
  }

  private Map<String, String> sentData() {
    ArgumentCaptor<PushRequest> captor = ArgumentCaptor.forClass(PushRequest.class);
    verify(dispatchService).dispatch(captor.capture());
    return captor.getValue().getData();
  }

  private Announcement announcement(String openYn) {
    return Announcement.builder().id(7L).organizationType("ECCLESIA").organizationId(10L)
        .openYn(openYn).build();
  }

  @Test
  void sendsToMyChurch_withGroups_andReturnsCounts() {
    PushDirectResponseDTO response = service.send(request("  주일   예배  안내 ", " 내일은 합심기도입니다 ",
        List.of(3L, 4L)));

    ArgumentCaptor<PushRequest> captor = ArgumentCaptor.forClass(PushRequest.class);
    verify(dispatchService).dispatch(captor.capture());
    PushRequest sent = captor.getValue();
    assertEquals(10L, sent.getEcclesiaUid());
    assertEquals(1L, sent.getSendAccountUid());
    assertEquals(List.of(3L, 4L), sent.getGroupIds());
    assertEquals("주일 예배 안내", sent.getTitle());
    assertEquals("내일은 합심기도입니다", sent.getMessage());
    assertEquals("DIRECT", sent.getCategory());

    assertEquals(55L, response.getPushId());
    assertEquals(10, response.getTargetCount());
    assertEquals(8, response.getSentCount());
    assertEquals(1, response.getFailedCount());
    assertEquals(1, response.getNoTokenCount());
  }

  @Test
  void layman_cannotSend() {
    loginAs(RoleType.LAYMAN);

    assertThrows(AccessDeniedException.class, () -> service.send(request("t", "m", null)));

    verify(dispatchService, never()).dispatch(any());
  }

  @Test
  void blankTitleOrMessage_isRejected() {
    assertThrows(IllegalArgumentException.class, () -> service.send(request("   ", "m", null)));
    assertThrows(IllegalArgumentException.class, () -> service.send(request("t", "  ", null)));

    verify(dispatchService, never()).dispatch(any());
  }

  @Test
  void noRecipients_isReportedAsError() {
    when(dispatchService.dispatch(any())).thenReturn(null);

    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> service.send(request("t", "m", List.of(3L))));

    assertEquals("알림을 받을 성도가 없습니다.", e.getMessage());
  }

  @Test
  void noRoute_sendsNoData() {
    service.send(requestWithRoute("  "));

    assertEquals(Map.of(), sentData());
  }

  @Test
  void fixedRoutes_areAllowed() {
    service.send(requestWithRoute("/church?tab=bulletin"));

    assertEquals(Map.of("route", "/church?tab=bulletin"), sentData());
  }

  @Test
  void ownOpenAnnouncement_isAllowed() {
    when(announcementRepository.findByIdAndOrganizationTypeAndOrganizationId(7L, "ECCLESIA", 10L))
        .thenReturn(Optional.of(announcement("Y")));

    service.send(requestWithRoute("/ecclesia/announcement/7"));

    assertEquals(Map.of("route", "/ecclesia/announcement/7"), sentData());
  }

  @Test
  void announcementOfAnotherChurchOrNotOpen_isRejected() {
    // 다른 교회 공지는 교회 id 조건 때문에 조회되지 않는다
    when(announcementRepository.findByIdAndOrganizationTypeAndOrganizationId(8L, "ECCLESIA", 10L))
        .thenReturn(Optional.empty());
    when(announcementRepository.findByIdAndOrganizationTypeAndOrganizationId(7L, "ECCLESIA", 10L))
        .thenReturn(Optional.of(announcement("N")));

    assertThrows(IllegalArgumentException.class,
        () -> service.send(requestWithRoute("/ecclesia/announcement/8")));
    assertThrows(IllegalArgumentException.class,
        () -> service.send(requestWithRoute("/ecclesia/announcement/7")));
    verify(dispatchService, never()).dispatch(any());
  }

  @Test
  void otherRoutes_areRejected() {
    for (String route : List.of("/myinfo", "https://evil.example", "/ecclesia/announcement/abc",
        "/ecclesia/announcement/7/../1", "/church?tab=other")) {
      assertThrows(IllegalArgumentException.class, () -> service.send(requestWithRoute(route)));
    }
    verify(dispatchService, never()).dispatch(any());
  }
}
