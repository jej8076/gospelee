package com.gospelee.api.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.announcement.AnnouncementResponseDTO;
import com.gospelee.api.dto.file.FileUploadResponseDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.Announcement;
import com.gospelee.api.entity.PushNotification;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.CategoryType;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.PushNotificationSendStatusType;
import com.gospelee.api.repository.PushNotificationReceiversRepository;
import com.gospelee.api.repository.PushNotificationRepository;
import com.gospelee.api.repository.announcement.AnnouncementRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

  private final AnnouncementRepository announcementRepository;
  private final PushNotificationRepository pushNotificationRepository;
  private final PushNotificationReceiversRepository pushNotificationReceiversRepository;
  private final AccountService accountService;
  private final FileService fileService;
  private final FirebaseService firebaseService;

  @Value("${server.domain:http://localhost:8008}")
  private String serverDomain;

  @Override
  public List<AnnouncementResponseDTO> getAnnouncementList(String announcementType) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    return announcementRepository.findByOrganizationTypeAndOrganizationId(
        OrganizationType.fromName(announcementType).name(), account.getEcclesiaUid());
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

    return AnnouncementDTO.fromEntity(announcement.get());
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
        announcementRepository.save(announcementDTO.toEntity(account)));

    // 파일 업로드 및 blob URL 매핑 생성
    Map<String, String> blobToFileUrlMap = new HashMap<>();

    if (files != null && !files.isEmpty()) {
      // blobFileMapping이 있는 경우 이를 활용
      Map<String, String> blobFileMapping = announcementDTO.getBlobFileMapping();

      for (MultipartFile file : files) {
        if (file != null && !file.isEmpty()) {
          FileUploadWrapperDTO fileUploadWrapper = FileUploadWrapperDTO.builder()
              .categoryType(CategoryType.ANNOUNCEMENT)
              .file(file)
              .accountAuth(account)
              .parentId(announcement.getId())
              .build();

          FileUploadResponseDTO uploadResult = fileService.uploadFileWithResponse(
              fileUploadWrapper);

          String fileUrl = serverDomain + "/api/file/secure/" + uploadResult.getAccessToken() + "/"
              + uploadResult.getFileDetailId();

          // blobFileMapping을 통해 blob URL과 파일명을 매핑
          if (blobFileMapping != null) {
            String originalFileName = file.getOriginalFilename();
            for (Map.Entry<String, String> entry : blobFileMapping.entrySet()) {
              if (entry.getValue().equals(originalFileName)) {
                blobToFileUrlMap.put(entry.getKey(), fileUrl);
                log.info("Blob URL 매핑 생성: {} -> {}", entry.getKey(), fileUrl);
                break;
              }
            }
          } else {
            // blobFileMapping이 없는 경우 기존 로직 사용 (순서대로 매핑)
            String originalFileName = file.getOriginalFilename();
            if (originalFileName != null) {
              // text에서 blob URL 패턴을 찾아서 순서대로 매핑
              String blobPattern = "blob:[^\"\\s]+";
              Pattern pattern = Pattern.compile(blobPattern);
              Matcher matcher = pattern.matcher(announcementDTO.getText());

              if (matcher.find()) {
                String blobUrl = matcher.group();
                if (!blobToFileUrlMap.containsKey(blobUrl)) {
                  blobToFileUrlMap.put(blobUrl, fileUrl);
                  log.info("Blob URL 매핑 생성 (자동): {} -> {}", blobUrl, fileUrl);
                }
              }
            }
          }
        }
      }
    }

    // text 내용의 blob URL을 실제 파일 URL로 치환
    String updatedText = replaceBlobUrls(announcementDTO.getText(), blobToFileUrlMap);

    // 업데이트된 text로 공지사항 다시 저장
    if (!updatedText.equals(announcementDTO.getText())) {
      announcementRepository.updateTextById(announcement.getId(), updatedText);
      announcement = AnnouncementDTO.builder()
          .id(announcement.getId())
          .organizationType(announcement.getOrganizationType())
          .organizationId(announcement.getOrganizationId())
          .subject(announcement.getSubject())
          .text(updatedText)
          .fileUid(announcement.getFileUid())
          .pushNotificationSendYn(announcement.getPushNotificationSendYn())
          .pushNotificationIds(announcement.getPushNotificationIds())
          .insertTime(announcement.getInsertTime())
          .updateTime(announcement.getUpdateTime())
          .build();
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


