package com.gospelee.api.service.bulletin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.bulletin.BulletinRequestDTO;
import com.gospelee.api.dto.bulletin.BulletinResponseDTO;
import com.gospelee.api.dto.file.FileUploadResponseDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.Bulletin;
import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.entity.FileEntity;
import com.gospelee.api.enums.RoleType;
import com.gospelee.api.repository.jpa.bulletin.BulletinRepository;
import com.gospelee.api.repository.jpa.file.FileDetailsRepository;
import com.gospelee.api.repository.jpa.file.FileRepository;
import com.gospelee.api.service.FileService;
import com.gospelee.api.service.announcement.AnnouncementStorageGuard;
import com.gospelee.api.service.push.PushDispatchService;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

class BulletinServiceTest {

  private static final byte[] PDF = "%PDF-1.7 body".getBytes();
  private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
  private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 1);

  private BulletinRepository bulletinRepository;
  private FileService fileService;
  private FileRepository fileRepository;
  private FileDetailsRepository fileDetailsRepository;
  private AnnouncementStorageGuard storageGuard;
  private PushDispatchService pushDispatchService;
  private BulletinService service;
  private MockedStatic<AuthenticatedUserUtils> auth;

  @BeforeEach
  void setUp() {
    bulletinRepository = mock(BulletinRepository.class);
    fileService = mock(FileService.class);
    fileRepository = mock(FileRepository.class);
    fileDetailsRepository = mock(FileDetailsRepository.class);
    storageGuard = mock(AnnouncementStorageGuard.class);
    pushDispatchService = mock(PushDispatchService.class);
    service = new BulletinService(bulletinRepository, fileService, fileRepository,
        fileDetailsRepository, storageGuard, pushDispatchService);
    auth = mockStatic(AuthenticatedUserUtils.class);
    loginAs(RoleType.PASTOR, 10L);
    when(bulletinRepository.save(any(Bulletin.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @AfterEach
  void tearDown() {
    auth.close();
  }

  private void loginAs(RoleType role, Long ecclesiaUid) {
    auth.when(AuthenticatedUserUtils::getAuthenticatedUserOrElseThrow).thenReturn(
        AccountAuthDTO.builder().uid(1L).role(role).ecclesiaUid(ecclesiaUid).build());
  }

  private BulletinRequestDTO request(Long id, List<Long> deleteIds) {
    return BulletinRequestDTO.builder().id(id).worshipDate(SUNDAY).title("11월 첫째 주")
        .deleteFileDetailIdList(deleteIds).build();
  }

  private MockMultipartFile pdf() {
    return new MockMultipartFile("files", "a.pdf", "text/html", PDF);
  }

  private MockMultipartFile png(String name) {
    return new MockMultipartFile("files", name, "image/png", PNG);
  }

  private FileDetails detail(long id, String ext, long size) {
    return FileDetails.builder().id(id).fileId(7L).fileSize(size).extension(ext)
        .fileOriginalName("f." + ext).delYn("N").build();
  }

  @Test
  void insert_layman_isDenied_andNothingIsReserved() {
    loginAs(RoleType.LAYMAN, 10L);

    assertThrows(AccessDeniedException.class,
        () -> service.insert(List.of(pdf()), request(null, null)));

    verify(storageGuard, never()).reserve(anyLong(), anyLong());
    verify(bulletinRepository, never()).save(any());
  }

  @Test
  void insert_mixedPdfAndImage_isRejectedBeforeReserve() {
    assertThrows(IllegalArgumentException.class,
        () -> service.insert(List.of(pdf(), png("a.png")), request(null, null)));

    verify(storageGuard, never()).reserve(anyLong(), anyLong());
  }

  @Test
  void insert_withoutFiles_isRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> service.insert(null, request(null, null)));
  }

  @Test
  void insert_duplicateDate_isRejected() {
    when(bulletinRepository.existsByEcclesiaUidAndWorshipDate(10L, SUNDAY)).thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> service.insert(List.of(pdf()), request(null, null)));

    verify(storageGuard, never()).reserve(anyLong(), anyLong());
  }

  @Test
  void insert_savesForMyChurch_reservesBytes_andForcesSafeContentType() {
    when(fileService.uploadFileWithResponse(any())).thenReturn(
        FileUploadResponseDTO.builder().fileId(7L).accessToken("tok").build());

    service.insert(List.of(pdf()), request(null, null));

    verify(storageGuard).reserve(10L, PDF.length);
    ArgumentCaptor<FileUploadWrapperDTO> captor = ArgumentCaptor.forClass(
        FileUploadWrapperDTO.class);
    verify(fileService).uploadFileWithResponse(captor.capture());
    // 클라이언트가 text/html 로 속여도 저장되는 contentType 은 서버가 판별한 값
    assertEquals("application/pdf", captor.getValue().getFiles().get(0).getContentType());
    ArgumentCaptor<Bulletin> saved = ArgumentCaptor.forClass(Bulletin.class);
    verify(bulletinRepository).save(saved.capture());
    assertEquals(10L, saved.getValue().getEcclesiaUid());
    assertEquals("Y", saved.getValue().getOpenYn());
    assertEquals(7L, saved.getValue().getFileUid());
  }

  @Test
  void insert_withPushYes_sendsToSelectedGroups() {
    when(fileService.uploadFileWithResponse(any())).thenReturn(
        FileUploadResponseDTO.builder().fileId(7L).accessToken("tok").build());
    BulletinRequestDTO dto = BulletinRequestDTO.builder().worshipDate(SUNDAY).title("t")
        .pushNotificationSendYn("Y").pushGroupIds(List.of(3L, 4L)).build();

    service.insert(List.of(pdf()), dto);

    verify(pushDispatchService).validateGroupIds(10L, List.of(3L, 4L));
    ArgumentCaptor<PushDispatchService.PushRequest> captor = ArgumentCaptor.forClass(
        PushDispatchService.PushRequest.class);
    verify(pushDispatchService).send(captor.capture());
    assertEquals(List.of(3L, 4L), captor.getValue().getGroupIds());
    assertEquals("BULLETIN", captor.getValue().getCategory());
    assertEquals(10L, captor.getValue().getEcclesiaUid());
  }

  @Test
  void insert_withInvalidGroup_isRejectedBeforeSaving() {
    when(pushDispatchService.validateGroupIds(10L, List.of(99L)))
        .thenThrow(new IllegalArgumentException("그룹"));
    BulletinRequestDTO dto = BulletinRequestDTO.builder().worshipDate(SUNDAY)
        .pushNotificationSendYn("Y").pushGroupIds(List.of(99L)).build();

    assertThrows(IllegalArgumentException.class, () -> service.insert(List.of(pdf()), dto));

    verify(bulletinRepository, never()).save(any());
    verify(storageGuard, never()).reserve(anyLong(), anyLong());
  }

  @Test
  void insert_withoutPush_orPrivateBulletin_sendsNothing() {
    when(fileService.uploadFileWithResponse(any())).thenReturn(
        FileUploadResponseDTO.builder().fileId(7L).accessToken("tok").build());

    service.insert(List.of(pdf()), request(null, null));
    service.insert(List.of(pdf()), BulletinRequestDTO.builder().worshipDate(SUNDAY.plusDays(7))
        .openYn("N").pushNotificationSendYn("Y").build());

    verify(pushDispatchService, never()).send(any());
  }

  @Test
  void update_otherChurchBulletin_isNotFound() {
    when(bulletinRepository.findByIdAndEcclesiaUid(5L, 10L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class,
        () -> service.update(null, request(5L, null)));
  }

  @Test
  void update_deleteIdsOutsideThisBulletin_areIgnored_andLastFileCannotBeRemoved() {
    Bulletin bulletin = Bulletin.builder().id(5L).ecclesiaUid(10L).worshipDate(SUNDAY)
        .fileUid(7L).openYn("Y").build();
    when(bulletinRepository.findByIdAndEcclesiaUid(5L, 10L)).thenReturn(Optional.of(bulletin));
    when(fileDetailsRepository.findAllByFileIdAndDelYn(7L, "N"))
        .thenReturn(List.of(detail(1L, "pdf", 100L)));

    // 999 는 이 주보의 파일이 아니므로 무시 -> PDF 가 그대로 남아 정상 처리
    service.update(null, request(5L, List.of(999L)));
    verify(fileDetailsRepository, never()).save(any(FileDetails.class));

    // 유일한 파일을 지우면서 새 파일도 없으면 거부
    assertThrows(IllegalArgumentException.class,
        () -> service.update(null, request(5L, List.of(1L))));
  }

  @Test
  void update_replacingPdfWithImages_releasesOldBytes() {
    Bulletin bulletin = Bulletin.builder().id(5L).ecclesiaUid(10L).worshipDate(SUNDAY)
        .fileUid(7L).openYn("Y").build();
    when(bulletinRepository.findByIdAndEcclesiaUid(5L, 10L)).thenReturn(Optional.of(bulletin));
    when(fileDetailsRepository.findAllByFileIdAndDelYn(7L, "N"))
        .thenReturn(List.of(detail(1L, "pdf", 100L)));
    when(fileService.uploadFileWithResponse(any())).thenReturn(
        FileUploadResponseDTO.builder().fileId(7L).accessToken("tok").build());

    service.update(List.of(png("1.png"), png("2.png")), request(5L, List.of(1L)));

    verify(storageGuard).reserve(10L, PNG.length * 2L);
    verify(storageGuard).release(10L, 100L);
  }

  @Test
  void delete_releasesBytes_andRemovesBulletin() {
    Bulletin bulletin = Bulletin.builder().id(5L).ecclesiaUid(10L).worshipDate(SUNDAY)
        .fileUid(7L).openYn("Y").build();
    when(bulletinRepository.findByIdAndEcclesiaUid(5L, 10L)).thenReturn(Optional.of(bulletin));
    when(fileDetailsRepository.findAllByFileIdAndDelYn(7L, "N"))
        .thenReturn(List.of(detail(1L, "png", 30L), detail(2L, "png", 20L)));

    service.delete(5L);

    verify(storageGuard).release(eq(10L), eq(50L));
    verify(bulletinRepository).delete(bulletin);
  }

  @Test
  void get_returnsFileListWithoutServerPath() {
    Bulletin bulletin = Bulletin.builder().id(5L).ecclesiaUid(10L).worshipDate(SUNDAY)
        .fileUid(7L).openYn("Y").build();
    when(bulletinRepository.findByIdAndEcclesiaUid(5L, 10L)).thenReturn(Optional.of(bulletin));
    when(fileDetailsRepository.findAllByFileIdAndDelYn(7L, "N"))
        .thenReturn(List.of(detail(1L, "png", 30L), detail(2L, "png", 20L)));
    FileEntity file = mock(FileEntity.class);
    when(file.getAccessToken()).thenReturn("tok");
    when(fileRepository.findByIdAndDelYn(7L, "N")).thenReturn(Optional.of(file));

    BulletinResponseDTO dto = service.get(5L);

    assertEquals("IMAGE", dto.getFileKind());
    assertEquals("tok", dto.getAccessToken());
    assertEquals(2, dto.getFileList().size());
  }

  @Test
  void get_accountWithoutChurch_isDenied() {
    loginAs(RoleType.LAYMAN, null);

    assertThrows(AccessDeniedException.class, () -> service.get(5L));
  }
}
