package com.gospelee.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaUpdateDTO;
import com.gospelee.api.dto.ecclesia.EcclesiaVerifyRequestDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountEcclesiaHistory;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.enums.AccountEcclesiaHistoryStatusType;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.exception.EcclesiaException;
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
import org.springframework.test.util.ReflectionTestUtils;

class EcclesiaRegistrationTest {

  private EcclesiaRepository ecclesiaRepository;
  private AccountEcclesiaHistoryRepository historyRepository;
  private AccountRepository accountRepository;
  private EcclesiaPushNotifier notifier;
  private EcclesiaServiceImpl service;
  private Account applicant;

  @BeforeEach
  void setUp() {
    ecclesiaRepository = mock(EcclesiaRepository.class);
    historyRepository = mock(AccountEcclesiaHistoryRepository.class);
    accountRepository = mock(AccountRepository.class);
    notifier = mock(EcclesiaPushNotifier.class);
    service = new EcclesiaServiceImpl(ecclesiaRepository, historyRepository,
        new AuthorizationService(), accountRepository, notifier);

    applicant = mock(Account.class);
    when(applicant.getName()).thenReturn("김목사");
    when(accountRepository.findById(7L)).thenReturn(Optional.of(applicant));
    when(ecclesiaRepository.findEcclesiasByMasterAccountUid(7L)).thenReturn(Optional.empty());
    when(ecclesiaRepository.save(any(Ecclesia.class))).thenAnswer(invocation -> {
      Ecclesia e = invocation.getArgument(0);
      return Ecclesia.builder().uid(5L).name(e.getName()).status(e.getStatus())
          .masterAccountUid(e.getMasterAccountUid()).build();
    });
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

  private EcclesiaInsertDTO insert(String name) {
    EcclesiaInsertDTO dto = mock(EcclesiaInsertDTO.class);
    when(dto.getName()).thenReturn(name);
    when(dto.getTelephone()).thenReturn("021234567");
    return dto;
  }

  @Test
  void register_approvesImmediately_grantsPastorRole_andNotifiesAdmins() {
    login(7L, RoleType.LAYMAN, null);

    Ecclesia saved = service.saveEcclesia(insert("  포도교회 "));

    assertEquals("APL", saved.getStatus());
    assertEquals("포도교회", saved.getName());
    assertFalse(saved.isVerified());
    assertEquals(7L, saved.getMasterAccountUid());
    verify(applicant).changeEcclesiaUid(5L);
    verify(applicant).changeRole(RoleType.SENIOR_PASTOR);
    verify(notifier).notifyChurchRegistered(any(Ecclesia.class), eq("김목사"));
  }

  @Test
  void register_blankName_isRejected() {
    login(7L, RoleType.LAYMAN, null);

    assertThrows(EcclesiaException.class, () -> service.saveEcclesia(insert("  ")));
    verify(ecclesiaRepository, never()).save(any(Ecclesia.class));
  }

  @Test
  void register_whenAlreadyMember_isRejected() {
    login(7L, RoleType.LAYMAN, 3L);

    assertThrows(EcclesiaException.class, () -> service.saveEcclesia(insert("포도교회")));
    verify(ecclesiaRepository, never()).save(any(Ecclesia.class));
  }

  @Test
  void register_whenAlreadyOwnsChurch_isRejected() {
    login(7L, RoleType.LAYMAN, null);
    when(ecclesiaRepository.findEcclesiasByMasterAccountUid(7L))
        .thenReturn(Optional.of(Ecclesia.builder().uid(1L).build()));

    assertThrows(EcclesiaException.class, () -> service.saveEcclesia(insert("포도교회")));
  }

  @Test
  void register_whenJoinRequestPending_isRejected() {
    login(7L, RoleType.LAYMAN, null);
    AccountEcclesiaHistory pending = mock(AccountEcclesiaHistory.class);
    when(pending.getStatus()).thenReturn(AccountEcclesiaHistoryStatusType.JOIN_REQUEST);
    when(historyRepository.findLatestByAccountUid(7L)).thenReturn(pending);

    assertThrows(EcclesiaException.class, () -> service.saveEcclesia(insert("포도교회")));
  }

  @Test
  void master_cannotChangeStatus_butAdminCan() {
    Ecclesia ecclesia = Ecclesia.builder().uid(5L).name("포도교회").status("APL")
        .masterAccountUid(7L).build();
    when(ecclesiaRepository.findById(5L)).thenReturn(Optional.of(ecclesia));
    EcclesiaUpdateDTO dto = mock(EcclesiaUpdateDTO.class);
    when(dto.getEcclesiaUid()).thenReturn(5L);
    when(dto.getStatus()).thenReturn("REJ");

    // 대표는 자기 교회라도 상태(승인/반려)를 바꿀 수 없다
    login(7L, RoleType.SENIOR_PASTOR, 5L);
    assertThrows(AccessDeniedException.class, () -> service.updateEcclesia(dto));
  }

  @Test
  void master_canStillUpdateAddress() {
    Ecclesia ecclesia = Ecclesia.builder().uid(5L).name("포도교회").status("APL")
        .masterAccountUid(7L).build();
    when(ecclesiaRepository.findById(5L)).thenReturn(Optional.of(ecclesia));
    when(ecclesiaRepository.save(any(Ecclesia.class))).thenAnswer(i -> i.getArgument(0));
    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(applicant));
    EcclesiaUpdateDTO dto = mock(EcclesiaUpdateDTO.class);
    when(dto.getEcclesiaUid()).thenReturn(5L);
    when(dto.getChurchAddress()).thenReturn("서울시");

    login(7L, RoleType.SENIOR_PASTOR, 5L);
    service.updateEcclesia(dto);

    assertEquals("서울시", ecclesia.getChurchAddress());
  }

  @Test
  void verify_onlyAdmin() {
    Ecclesia ecclesia = Ecclesia.builder().uid(5L).name("포도교회").status("APL")
        .masterAccountUid(7L).build();
    when(ecclesiaRepository.findById(5L)).thenReturn(Optional.of(ecclesia));
    when(ecclesiaRepository.save(any(Ecclesia.class))).thenAnswer(i -> i.getArgument(0));
    EcclesiaVerifyRequestDTO request = mock(EcclesiaVerifyRequestDTO.class);
    when(request.getEcclesiaUid()).thenReturn(5L);
    when(request.getVerified()).thenReturn(true);

    login(7L, RoleType.SENIOR_PASTOR, 5L);
    assertThrows(AccessDeniedException.class, () -> service.updateVerification(request));
    assertFalse(ecclesia.isVerified());

    login(1L, RoleType.ADMIN, null);
    service.updateVerification(request);
    assertTrue(ecclesia.isVerified());
  }

  @Test
  void joinRequestBySearch_toUnverifiedChurch_isRejected() {
    login(8L, RoleType.LAYMAN, null);
    Ecclesia unverified = Ecclesia.builder().uid(5L).name("포도교회").status("APL").build();
    when(ecclesiaRepository.findById(5L)).thenReturn(Optional.of(unverified));

    assertThrows(EcclesiaException.class, () -> service.joinRequestEcclesia(5L));
    verify(historyRepository, never()).save(any(AccountEcclesiaHistory.class));
  }

  @Test
  void joinRequestBySearch_toVerifiedChurch_isAccepted() {
    login(8L, RoleType.LAYMAN, null);
    Ecclesia verified = Ecclesia.builder().uid(5L).name("포도교회").status("APL").build();
    verified.changeVerified(true);
    when(ecclesiaRepository.findById(5L)).thenReturn(Optional.of(verified));
    when(historyRepository.save(any(AccountEcclesiaHistory.class)))
        .thenAnswer(i -> i.getArgument(0));

    service.joinRequestEcclesia(5L);

    verify(historyRepository).save(any(AccountEcclesiaHistory.class));
    verify(notifier).notifyJoinRequested(eq(verified), any());
  }

  @Test
  void joinByInvite_unverifiedChurchAt30Members_isRejected() {
    login(8L, RoleType.LAYMAN, null);
    Ecclesia unverified = Ecclesia.builder().uid(5L).name("포도교회").status("APL").build();
    ReflectionTestUtils.setField(unverified, "insertTime", java.time.LocalDateTime.now());
    when(ecclesiaRepository.findByInviteCode("abc234abc234")).thenReturn(Optional.of(unverified));
    when(accountRepository.countByEcclesiaUid(5L)).thenReturn(30L);

    assertThrows(EcclesiaException.class, () -> service.joinByInvite("abc234abc234"));
    verify(historyRepository, never()).save(any(AccountEcclesiaHistory.class));
  }

  @Test
  void joinByInvite_afterVerificationDeadline_isRejected() {
    login(8L, RoleType.LAYMAN, null);
    Ecclesia expired = Ecclesia.builder().uid(5L).name("포도교회").status("APL").build();
    ReflectionTestUtils.setField(expired, "insertTime",
        java.time.LocalDateTime.now().minusDays(15));
    when(ecclesiaRepository.findByInviteCode("abc234abc234")).thenReturn(Optional.of(expired));

    assertThrows(EcclesiaException.class, () -> service.joinByInvite("abc234abc234"));
  }

  @Test
  void joinByInvite_unverifiedChurchUnderLimit_isAccepted() {
    login(8L, RoleType.LAYMAN, null);
    Ecclesia unverified = Ecclesia.builder().uid(5L).name("포도교회").status("APL").build();
    ReflectionTestUtils.setField(unverified, "insertTime", java.time.LocalDateTime.now());
    when(ecclesiaRepository.findByInviteCode("abc234abc234")).thenReturn(Optional.of(unverified));
    when(accountRepository.countByEcclesiaUid(5L)).thenReturn(29L);
    when(historyRepository.save(any(AccountEcclesiaHistory.class)))
        .thenAnswer(i -> i.getArgument(0));

    assertEquals("PENDING", service.joinByInvite("abc234abc234").getStatus());
  }
}
