package com.gospelee.api.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.dto.file.FileDetailsDTO;
import com.gospelee.api.dto.file.FileUploadResponseDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.entity.FileEntity;
import com.gospelee.api.entity.PushNotification;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.CategoryType;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.PushNotificationSendStatusType;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.repository.jpa.file.FileDetailsRepository;
import com.gospelee.api.repository.jpa.file.FileRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

  private final AnnouncementRepository announcementRepository;
  private final PushNotificationRepository pushNotificationRepository;
  private final PushNotificationReceiversRepository pushNotificationReceiversRepository;
  private final AccountService accountService;
  private final FileService fileService;
  private final FirebaseService firebaseService;
  private final FileDetailsRepository fileDetailsRepository;
  private final FileRepository fileRepository;

  @Value("${auth.super-id:admin}")
  private String superId;

  @Override
  public List<AnnouncementResponseDTO> getAnnouncementList(String announcementType) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    List<AnnouncementResponseDTO> responseList;

    if (account.getEmail().equals(superId)) {
      responseList = announcementRepository.findByOrganizationType(announcementType).stream()
          .map(AnnouncementResponseDTO::fromEntity).collect(
              Collectors.toList());
    } else {
      responseList = announcementRepository.findByOrganizationTypeAndOrganizationId(
          OrganizationType.fromName(announcementType).name(), account.getEcclesiaUid());

    }

    for (AnnouncementResponseDTO ann : responseList) {
      if (ann.getFileUid() == null) {
        continue;
      }
      Optional<FileEntity> file = fileRepository.findByIdAndDelYn(ann.getFileUid(), Yn.N.name());
      if (file.isEmpty()) {
        continue;
      }
      ann.changeImageAccessToken(file.get().getAccessToken());

      List<FileDetails> fileDetailList = fileDetailsRepository.findAllByFileId(
          ann.getFileUid());

      List<FileDetailsDTO> fileDetailDtoList = fileDetailList.stream()
          .map(FileDetailsDTO::fromEntity)
          .toList();

      ann.changeFileDetail(fileDetailDtoList);
    }

    return responseList;
  }

  @Override
  public AnnouncementDTO getAnnouncement(String announcementType, Long id) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    // 임시로 ecclesia 공지사항만 고려함
    Optional<Announcement> announcement = announcementRepository.findByIdAndOrganizationTypeAndOrganizationId(
        id, announcementType, account.getEcclesiaUid());

    if (announcement.isEmpty()) {
      throw new RuntimeException("실패함");
    }

    List<FileDetails> fileDetailList = fileDetailsRepository.findAllByFileId(
        announcement.get().getFileUid());

    List<FileDetailsDTO> fileDetailDtoList = fileDetailList.stream()
        .map(FileDetailsDTO::fromEntity)
        .toList();

    return AnnouncementDTO.fromEntity(announcement.get(), fileDetailDtoList);
  }

  /**
   * <pre>
   * 1. 공지사항 데이터 저장
   * 2. file 데이터 저장 -> file 실제로 저장
   * 3. text 내용의 blob URL을 실제 파일 URL로 치환
   * 4. pushNotification 데이터 저장
   * 5. pushNotification 발송 요청
   * 6. pushNotification detail 데이터 저장
   * 7. pushNotification 데이터에 totalCount 업데이트
   * 8. announcement 데이터에 pushNotification Id를 저장
   * </pre>
   *
   * @param files
   * @param announcementDTO
   * @return
   */
  @Override
  @Transactional
  public AnnouncementDTO insertAnnouncement(List<MultipartFile> files,
      AnnouncementDTO announcementDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    // 공지사항 데이터 저장 (임시)
    AnnouncementDTO announcement = AnnouncementDTO.fromEntity(
        announcementRepository.save(announcementDTO.toEntity(account)), null);

    if (files != null && !files.isEmpty()) {
      FileUploadWrapperDTO fileUploadWrapper = FileUploadWrapperDTO.builder()
          .categoryType(CategoryType.ANNOUNCEMENT)
          .files(files)
          .accountAuth(account)
          .parentId(announcement.getId())
          .build();

      FileUploadResponseDTO fileUploadResponse = fileService.uploadFileWithResponse(
          fileUploadWrapper);

      // 업로드한 fileId announcement 에 입력
      announcementRepository.updateFileId(announcement.getId(), fileUploadResponse.getFileId());
    }

    // push 알림을 활성화하지 않으면 발송안함
    if ("N".equals(announcementDTO.getPushNotificationSendYn())) {
      return announcement;
    }

    Optional<List<Account>> accounts = accountService.getAccountByEcclesiaUid(
        account.getEcclesiaUid());

    // 발송 대상이 없으면 발송 안함
    if (accounts.isEmpty()) {
      return announcement;
    }

    PushNotification pushNotification = PushNotification.builder()
        .sendAccountUid(account.getUid())
        .organization(OrganizationType.fromName(announcement.getOrganizationType()).name())
        .category(CategoryType.ANNOUNCEMENT.name())
        .title("교회에서 공지사항을 등록했습니다.")
        .message("공지사항을 확인해주세요.")
        .build();

    pushNotification = pushNotificationRepository.save(pushNotification);

    List<PushNotificationReceivers> pushNotificationReceiversList = new ArrayList<>();
    for (Account acc : accounts.get()) {
      // 발송 대상의 push token이 없으면 발송안함
      if (acc.getPushToken() == null || acc.getPushToken().isEmpty()) {
        continue;
      }

      try {
        firebaseService.sendNotification(acc.getPushToken(), pushNotification.getTitle(),
            pushNotification.getMessage());
      } catch (FirebaseMessagingException e) {
        throw new RuntimeException(e);
      }

      PushNotificationReceivers pushNotificationReceivers = PushNotificationReceivers.builder()
          .pushNotificationId(pushNotification.getId())
          .receiveAccountUid(acc.getUid())
          .status(PushNotificationSendStatusType.READY.name())
          .build();

      pushNotificationReceiversList.add(pushNotificationReceivers);
    }

    List<PushNotificationReceivers> resultList = pushNotificationReceiversRepository.saveAll(
        pushNotificationReceiversList);

    pushNotificationRepository.updateTotalcountById(pushNotification.getId(), resultList.size());

    announcementRepository.updatePushNotificationIdsById(announcement.getId(),
        String.valueOf(pushNotification.getId()));

    return announcement;
  }

  @Override
  public AnnouncementDTO updateAnnouncement(List<MultipartFile> files,
      AnnouncementDTO announcementDTO) {

    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    // 공지사항 데이터 저장 (임시)
    AnnouncementDTO announcement = AnnouncementDTO.fromEntity(
        announcementRepository.save(announcementDTO.toEntity(account)), null);

    if (files != null && !files.isEmpty()) {
      return announcement;
    }

    Optional<FileEntity> fileEntity = fileService.getFileEntity(announcement.getFileUid());

    if (fileEntity.isEmpty()) {
      // insert
      FileUploadWrapperDTO fileUploadWrapper = FileUploadWrapperDTO.builder()
          .categoryType(CategoryType.ANNOUNCEMENT)
          .files(files)
          .accountAuth(account)
          .parentId(announcement.getId())
          .build();

      FileUploadResponseDTO fileUploadResponse = fileService.uploadFileWithResponse(
          fileUploadWrapper);

      // 업로드한 fileId announcement 에 입력
      announcementRepository.updateFileId(announcement.getId(), fileUploadResponse.getFileId());
    } else {
      // update
      fileService.replaceFileWithResponse(fileEntity.get(), files);
    }

    return null;
  }

  /**
   * text 내용의 blob URL을 실제 파일 URL로 치환
   *
   * @param text             원본 텍스트
   * @param blobToFileUrlMap blob URL과 실제 파일 URL의 매핑
   * @return 치환된 텍스트
   */
  private String replaceBlobUrls(String text, Map<String, String> blobToFileUrlMap) {
    if (text == null || blobToFileUrlMap.isEmpty()) {
      return text;
    }

    String result = text;
    for (Map.Entry<String, String> entry : blobToFileUrlMap.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }

    return result;
  }
}


