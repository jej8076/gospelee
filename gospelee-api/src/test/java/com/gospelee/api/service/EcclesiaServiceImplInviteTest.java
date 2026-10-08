package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInviteDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.AccountEcclesiaHistoryRepository;
import com.gospelee.api.repository.EcclesiaRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class EcclesiaServiceImplInviteTest {

  private EcclesiaRepository ecclesiaRepository;
  private AuthorizationService authorizationService;
  private EcclesiaServiceImpl service;

  @BeforeEach
  void setUp() {
    ecclesiaRepository = mock(EcclesiaRepository.class);
    authorizationService = new AuthorizationService();
    service = new EcclesiaServiceImpl(ecclesiaRepository,
        mock(AccountEcclesiaHistoryRepository.class), authorizationService,
        mock(AccountRepository.class), mock(EcclesiaPushNotifier.class),
        mock(SlackNotifier.class));

    Ecclesia ecclesia = Ecclesia.builder()
        .uid(10L)
        .name("포도교회")
        .status("APL")
        .masterAccountUid(1L)
        .build();
    when(ecclesiaRepository.findById(10L)).thenReturn(Optional.of(ecclesia));
    when(ecclesiaRepository.save(any(Ecclesia.class))).thenAnswer(i -> i.getArgument(0));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void login(long uid, RoleType role, Long ecclesiaUid) {
    AccountAuthDTO account = AccountAuthDTO.builder()
        .uid(uid).role(role).ecclesiaUid(ecclesiaUid).build();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities()));
  }

  @Test
  void master_canGetInvite_andCodeIsCreated() {
    login(1L, RoleType.SENIOR_PASTOR, 10L);

    EcclesiaInviteDTO invite = service.getInvite();

    assertNotNull(invite.getInviteCode());
    assertEquals(12, invite.getInviteCode().length());
  }

  @Test
  void nonMasterSeniorPastor_ofSameChurch_canGetInvite() {
    login(2L, RoleType.SENIOR_PASTOR, 10L);

    assertNotNull(service.getInvite().getInviteCode());
  }

  @Test
  void pastor_canUpdateSettings() {
    login(3L, RoleType.PASTOR, 10L);

    assertEquals(true, service.updateInviteSettings(true).isAutoApprove());
  }

  @Test
  void layman_isDenied() {
    login(4L, RoleType.LAYMAN, 10L);

    assertThrows(AccessDeniedException.class, () -> service.getInvite());
    assertThrows(AccessDeniedException.class, () -> service.regenerateInvite());
    assertThrows(AccessDeniedException.class, () -> service.updateInviteSettings(true));
  }
}
