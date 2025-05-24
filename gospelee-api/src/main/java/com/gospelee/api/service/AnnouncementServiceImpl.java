package com.gospelee.api.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.announcement.AnnouncementDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.PushNotification;
import com.gospelee.api.entity.PushNotificationReceivers;
import com.gospelee.api.enums.FileCategoryType;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.PushNotificationSendStatusType;
import com.gospelee.api.repository.AnnouncementRepository;
import com.gospelee.api.repository.PushNotificationReceiversRepository;
import com.gospelee.api.repository.PushNotificationRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

  private final AnnouncementRepository announcementRepository;
  private final PushNotificationRepository pushNotificationRepository;
  private final PushNotificationReceiversRepository pushNotificationReceiversRepository;
  private final AccountService accountService;
  private final FileService fileService;
  private final FirebaseService firebaseService;

  @Override
  public List<AnnouncementDTO> getAnnouncementList() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    // TODO 임시코드임
    return Collections.singletonList(AnnouncementDTO.builder().build());
  }

  /**
   * <pre>
   * 1. 공지사항 데이터 저장
   * 2. file 데이터 저장 -> file 실제로 저장
   * 3. pushNotification 데이터 저장
   * 4. pushNotification 발송 요청
   * 5. pushNotification detail 데이터 저장
   * 6. pushNotification 데이터에 totalCount 업데이트
   * 7. announcement 데이터에 pushNotification Id를 저장
   * </pre>
   *
   * @param file
   * @param announcementDTO
   * @return
   */
  @Override
  @Transactional
  public AnnouncementDTO insertAnnouncement(MultipartFile file, AnnouncementDTO announcementDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

    // 공지사항 데이터 저장
    AnnouncementDTO announcement = AnnouncementDTO.fromEntity(
        announcementRepository.save(announcementDTO.toEntity()));

    // file upload
    if (file != null) {
      FileUploadWrapperDTO fileUploadWrapper = FileUploadWrapperDTO.builder()
          .fileCategoryType(FileCategoryType.fromName(announcementDTO.getOrganizationType()))
          .file(file)
          .accountAuth(account)
          .parentId(announcement.getId())
          .build();
      fileService.uploadFile(fileUploadWrapper);
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
        .category(OrganizationType.fromName(announcement.getOrganizationType()).name())
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
}


