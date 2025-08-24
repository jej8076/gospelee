package com.gospelee.api.service.announcement;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.dto.announcement.BlobToFileUrlMappingRequestDTO;
import com.gospelee.api.dto.file.FileDetailsDTO;
import com.gospelee.api.dto.file.FileUploadDetailResponseDTO;
import com.gospelee.api.dto.file.FileUploadResponseDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.entity.FileEntity;
import com.gospelee.api.entity.PushNotification;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.AppType;
import com.gospelee.api.enums.CategoryType;
import com.gospelee.api.enums.CustomHeader;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.PushNotificationSendStatusType;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.exception.AnnouncementNotFoundException;
import com.gospelee.api.exception.SuperAccountException;
import com.gospelee.api.repository.jpa.announcement.AnnouncementRepository;
import com.gospelee.api.repository.jpa.file.FileDetailsRepository;
import com.gospelee.api.repository.jpa.file.FileRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationReceiversRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushNotificationRepository;
import com.gospelee.api.service.AccountService;
import com.gospelee.api.service.FileService;
import com.gospelee.api.service.FirebaseService;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
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

@Service("AnnouncementAdmin")
@Slf4j
@RequiredArgsConstructor
public class AnnouncementServiceAdminImpl implements AnnouncementService {

  private final AnnouncementRepository announcementRepository;
  private final PushNotificationRepository pushNotificationRepository;
  private final PushNotificationReceiversRepository pushNotificationReceiversRepository;
  private final AccountService accountService;
  private final FileService fileService;
  private final FirebaseService firebaseService;
  private final FileDetailsRepository fileDetailsRepository;
  private final FileRepository fileRepository;

  @Value("${server.domain:http://localhost:8008}")
  private String serverDomain;

  @Value("${auth.super-id:admin}")
  private String superId;


  @Override
  public List<AnnouncementResponseDTO> getAnnouncementList(HttpServletRequest request,
      String announcementType) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    List<AnnouncementResponseDTO> responseList;

    if (account.getEmail().equals(superId)) {
      responseList = announcementRepository.findByOrganizationType(announcementType).stream()
          .map(AnnouncementResponseDTO::fromEntity).collect(
              Collectors.toList());
    } else {
      String appIdentify = request.getHeader(CustomHeader.X_APP_IDENTIFIER.getHeaderName());
      OrganizationType organizationType = OrganizationType.fromName(announcementType);

      // OOG_WEB 헤더가 아니면 open된 목록만 조회하도록 의도함
      boolean isOpen = !AppType.OOG_WEB.getValue().equals(appIdentify);

      if (organizationType.equals(OrganizationType.BRAND_STORY)) {
        // BRAND_STORY는 organization_id를 1로 고정함
        responseList = announcementRepository.findByOrganizationTypeAndOrganizationIdAndOpenY(
            organizationType.name(), 1L, isOpen);

      } else if (organizationType.equals(OrganizationType.ECCLESIA)) {
        if (account.getEcclesiaUid() == null) {
          return List.of();
        }
        responseList = announcementRepository.findByOrganizationTypeAndOrganizationIdAndOpenY(
            organizationType.name(), account.getEcclesiaUid(), isOpen);

      } else {
        throw new AnnouncementNotFoundException("not_found_organization_type organizationType:{}",
            organizationType.name());
      }
    }

    for (AnnouncementResponseDTO response : responseList) {
      if (response.getFileUid() == null) {
        continue;
      }
      Optional<FileEntity> file = fileRepository.findByIdAndDelYn(response.getFileUid(),
          Yn.N.name());
      if (file.isEmpty()) {
        continue;
      }
      response.changeImageAccessToken(file.get().getAccessToken());

      List<FileDetails> fileDetailList = fileDetailsRepository.findAllByFileIdAndDelYn(
          response.getFileUid(), Yn.N.name());

      List<FileDetailsDTO> fileDetailDtoList = fileDetailList.stream()
          .map(FileDetailsDTO::fromEntity)
          .toList();

      response.changeFileDetail(fileDetailDtoList);
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

    Announcement announcementEntity = announcement.get();
    List<FileDetailsDTO> fileDetailDtoList = new ArrayList<>();
    List<String> fileDataList = new ArrayList<>();

    // 파일이 있는 경우 파일 정보와 데이터를 함께 조회
    if (announcementEntity.getFileUid() != null) {
      List<FileDetails> fileDetailList = fileDetailsRepository.findAllByFileIdAndDelYn(
          announcementEntity.getFileUid(), Yn.N.name());

      fileDetailDtoList = fileDetailList.stream()
          .map(FileDetailsDTO::fromEntity)
          .toList();

      // 각 파일에 대해 Resource를 조회하여 Base64로 인코딩
      for (FileDetails fileDetail : fileDetailList) {
        try {
          org.springframework.core.io.Resource resource = fileService.getFile(
              announcementEntity.getFileUid(), fileDetail.getId());

          // Resource를 Base64로 인코딩
          byte[] fileBytes = resource.getInputStream().readAllBytes();
          String base64Data = java.util.Base64.getEncoder().encodeToString(fileBytes);
          fileDataList.add(base64Data);

        } catch (Exception e) {
          log.warn("파일 리소스 조회 실패 - fileId: {}, fileDetailId: {}, error: {}",
              announcementEntity.getFileUid(), fileDetail.getId(), e.getMessage());
          // 파일 조회 실패 시에도 계속 진행 (빈 문자열 추가)
          fileDataList.add("");
        }
      }
    }

    return AnnouncementDTO.fromEntity(announcementEntity, fileDetailDtoList, fileDataList);
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

    if (!isOnlySuperValidation(account, announcementDTO)) {
      throw new SuperAccountException("invalid_access accountEmail:{}", account.getEmail());
    }

    // 1. 저장
    Announcement announcement = announcementRepository.save(announcementDTO.toEntity(account));

    // 2. 파일 업데이트 처리
    uploadNewFiles(announcement, announcementDTO, files, account);

    // 3. 푸시 알림 처리
    handlePushNotificationIfNeeded(announcement, announcementDTO, account);

    return AnnouncementDTO.fromEntity(announcement);

  }

  @Override
  @Transactional
  public AnnouncementDTO updateAnnouncement(List<MultipartFile> files,
      AnnouncementDTO announcementDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    if (!isOnlySuperValidation(account, announcementDTO)) {
      throw new SuperAccountException("invalid_access accountEmail:{}", account.getEmail());
    }

    // 1. 기존 데이터 조회 및 검증
    Announcement existingAnnouncement = getExistingAnnouncementOrThrow(announcementDTO.getId());

    // 2. 기본 필드 업데이트
    updateBasicFields(existingAnnouncement, announcementDTO);

    // 3. 공지사항 저장
    Announcement savedAnnouncement = announcementRepository.save(existingAnnouncement);

    // 4. 파일 업데이트 처리
    uploadNewFiles(savedAnnouncement, announcementDTO, files, account);

    // 5. 기존 파일 삭제 처리
    deleteExistingFiles(announcementDTO.getDeleteFileDetailIdList());

    // 6. 푸시 알림 처리
    handlePushNotificationIfNeeded(savedAnnouncement, announcementDTO, account);

    return AnnouncementDTO.fromEntity(savedAnnouncement);
  }

  private void updateBasicFields(Announcement existing, AnnouncementDTO updates) {
    if (updates.getSubject() != null) {
      existing.changeSubject(updates.getSubject());
    }
    if (updates.getText() != null) {
      existing.changeText(updates.getText());
    }
    if (updates.getOpenYn() != null) {
      existing.changeOpenYn(Yn.of(updates.getOpenYn()));
    }

  }

  private void deleteExistingFiles(List<Long> deleteFileDetailIdList) {
    if (deleteFileDetailIdList == null || deleteFileDetailIdList.isEmpty()) {
      return;
    }

    log.info("기존 파일 삭제 요청 - fileDetailIds: {}", deleteFileDetailIdList);

    for (Long fileDetailId : deleteFileDetailIdList) {
      try {
        // FileDetails 조회
        Optional<FileDetails> fileDetailOpt = fileDetailsRepository.findById(fileDetailId);
        if (fileDetailOpt.isPresent()) {
          FileDetails fileDetail = fileDetailOpt.get();

          // 파일 삭제 마킹 (실제 파일 삭제는 별도 배치에서 처리하거나 즉시 처리)
          fileDetail.markAsDeleted(); // 이 메서드가 FileDetails 엔티티에 있다고 가정
          fileDetailsRepository.save(fileDetail);

          log.info("파일 삭제 완료 - fileDetailId: {}, fileName: {}",
              fileDetailId, fileDetail.getFileOriginalName());
        } else {
          log.warn("삭제할 파일을 찾을 수 없음 - fileDetailId: {}", fileDetailId);
        }
      } catch (Exception e) {
        log.error("파일 삭제 실패 - fileDetailId: {}, error: {}", fileDetailId, e.getMessage());
        // 파일 삭제 실패해도 전체 프로세스는 계속 진행
      }
    }
  }

  private void handlePushNotificationIfNeeded(Announcement announcement,
      AnnouncementDTO announcementDTO,
      AccountAuthDTO account) {

    if (!"Y".equals(announcementDTO.getPushNotificationSendYn())) {
      return;
    }

    Optional<List<Account>> accounts = accountService.getAccountByEcclesiaUid(
        account.getEcclesiaUid());

    // 발송 대상이 없으면 발송 안함
    if (accounts.isEmpty()) {
      return;
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

  }

  private Announcement getExistingAnnouncementOrThrow(Long id) {

    Optional<Announcement> orgAnnouncement = announcementRepository.findById(id);

    if (orgAnnouncement.isEmpty()) {
      throw new AnnouncementNotFoundException("announcement 데이터 없음 -> id:{}", id);
    }

    return orgAnnouncement.get();

  }

  private void uploadNewFiles(Announcement announcement, AnnouncementDTO announcementDTO,
      List<MultipartFile> files, AccountAuthDTO account) {

    if (files == null || files.isEmpty()) {
      return;
    }

    FileUploadWrapperDTO fileUploadWrapper = FileUploadWrapperDTO.builder()
        .fileId(announcement.getFileUid())
        .categoryType(CategoryType.ANNOUNCEMENT)
        .files(files)
        .accountAuth(account)
        .parentId(announcement.getId())
        .build();

    FileUploadResponseDTO fileUploadResponse = fileService.uploadFileWithResponse(
        fileUploadWrapper);

    // 업로드한 fileId announcement 에 입력
    announcementRepository.updateFileId(announcement.getId(), fileUploadResponse.getFileId());

    // 본문 내용에 있는 img태그의 url을 치환하기 위함
    BlobToFileUrlMappingRequestDTO mappingRequestDTO = BlobToFileUrlMappingRequestDTO.builder()
        .fileDetailList(fileUploadResponse.getFileDetailList())
        .blobFileMapping(announcementDTO.getBlobFileMapping())
        .serverDomain(serverDomain)
        .accessToken(fileUploadResponse.getAccessToken())
        .build();
    Map<String, String> mappingResult = generateBlobToFileUrlMap(mappingRequestDTO);

    // text 내용의 blob URL을 실제 파일 URL로 치환
    String updatedText = replaceBlobUrls(announcementDTO.getText(), mappingResult);

    // 업데이트된 text로 공지사항 다시 저장
    if (!updatedText.equals(announcementDTO.getText())) {
      announcementRepository.updateTextById(announcement.getId(), updatedText);
      announcement.changeText(updatedText);
    }
  }

  private void updateNewFiles(Announcement announcement, AnnouncementDTO announcementDTO,
      List<MultipartFile> files, AccountAuthDTO account) {

    if (files == null || files.isEmpty()) {
      return;
    }

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

    // 본문 내용에 있는 img태그의 url을 치환하기 위함
    BlobToFileUrlMappingRequestDTO mappingRequestDTO = BlobToFileUrlMappingRequestDTO.builder()
        .fileDetailList(fileUploadResponse.getFileDetailList())
        .blobFileMapping(announcementDTO.getBlobFileMapping())
        .serverDomain(serverDomain)
        .accessToken(fileUploadResponse.getAccessToken())
        .build();
    Map<String, String> mappingResult = generateBlobToFileUrlMap(mappingRequestDTO);

    // text 내용의 blob URL을 실제 파일 URL로 치환
    String updatedText = replaceBlobUrls(announcementDTO.getText(), mappingResult);

    // 업데이트된 text로 공지사항 다시 저장
    if (!updatedText.equals(announcementDTO.getText())) {
      announcementRepository.updateTextById(announcement.getId(), updatedText);
      announcement.changeText(updatedText);
    }
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

  public Map<String, String> generateBlobToFileUrlMap(
      BlobToFileUrlMappingRequestDTO request) {
    Map<String, String> blobToFileUrlMap = new HashMap<>();

    List<FileUploadDetailResponseDTO> fileDetails = request.getFileDetailList();
    Map<String, String> blobFileMapping = request.getBlobFileMapping();

    if (fileDetails == null || blobFileMapping == null) {
      return blobToFileUrlMap;
    }

    for (FileUploadDetailResponseDTO dto : fileDetails) {
      String fileUrl = request.getServerDomain()
          + "/api/file/"
          + request.getAccessToken()
          + "/"
          + dto.getFileDetailId();

      String originalFileName = dto.getFileOriginalName();

      for (Map.Entry<String, String> entry : blobFileMapping.entrySet()) {
        if (entry.getValue().equals(originalFileName)) {
          blobToFileUrlMap.put(entry.getKey(), fileUrl);
          log.info("Blob URL 매핑 생성: {} -> {}", entry.getKey(), fileUrl);
          break;
        }
      }
    }

    return blobToFileUrlMap;
  }

  private boolean isOnlySuperValidation(AccountAuthDTO account, AnnouncementDTO announcementDTO) {
    if (announcementDTO.getOrganizationType().equals(OrganizationType.BRAND_STORY.name())
        && !superId.equals(account.getEmail())) {
      return false;
    }

    return true;
  }
}


