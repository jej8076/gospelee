package com.gospelee.api.service.bulletin;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.bulletin.BulletinFileDTO;
import com.gospelee.api.dto.bulletin.BulletinRequestDTO;
import com.gospelee.api.dto.bulletin.BulletinResponseDTO;
import com.gospelee.api.dto.file.FileUploadResponseDTO;
import com.gospelee.api.dto.file.FileUploadWrapperDTO;
import com.gospelee.api.entity.Bulletin;
import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.enums.AppType;
import com.gospelee.api.enums.CategoryType;
import com.gospelee.api.enums.OrganizationType;
import com.gospelee.api.enums.CustomHeader;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.repository.jpa.bulletin.BulletinRepository;
import com.gospelee.api.repository.jpa.file.FileDetailsRepository;
import com.gospelee.api.repository.jpa.file.FileRepository;
import com.gospelee.api.service.FileService;
import com.gospelee.api.service.bulletin.BulletinFilePolicy.Kind;
import com.gospelee.api.service.announcement.AnnouncementAccessPolicy;
import com.gospelee.api.service.announcement.AnnouncementStorageGuard;
import com.gospelee.api.service.push.PushDispatchService;
import com.gospelee.api.service.push.PushDispatchService.PushRequest;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 교회 주보. 조회는 같은 교회 교인만, 등록/수정/삭제는 같은 교회의 관리자만 가능하다.
 * 첨부 파일 용량은 공지와 같은 교회 저장 공간에 포함한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulletinService {

  static final int DEFAULT_LIMIT = 20;
  static final int MAX_LIMIT = 52;

  private final BulletinRepository bulletinRepository;
  private final FileService fileService;
  private final FileRepository fileRepository;
  private final FileDetailsRepository fileDetailsRepository;
  private final AnnouncementStorageGuard storageGuard;
  private final PushDispatchService pushDispatchService;

  /** 내 교회 주보 목록 (예배 날짜 최신순). 앱은 공개된 것만, 관리자 웹은 전체를 본다 */
  @Transactional(readOnly = true)
  public List<BulletinResponseDTO> getList(HttpServletRequest request, Integer limit) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (account.getEcclesiaUid() == null) {
      return List.of();
    }
    PageRequest page = PageRequest.of(0, clampLimit(limit));
    boolean web = AppType.PODO_WEB.getValue()
        .equals(request.getHeader(CustomHeader.X_APP_IDENTIFIER.getHeaderName()));

    List<Bulletin> bulletins = web
        ? bulletinRepository.findByEcclesiaUidOrderByWorshipDateDesc(account.getEcclesiaUid(), page)
        : bulletinRepository.findByEcclesiaUidAndOpenYnOrderByWorshipDateDesc(
            account.getEcclesiaUid(), Yn.Y.name(), page);
    return bulletins.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public BulletinResponseDTO get(Long id) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (account.getEcclesiaUid() == null) {
      throw new AccessDeniedException("소속 교회가 없습니다.");
    }
    Bulletin bulletin = bulletinRepository.findByIdAndEcclesiaUid(id, account.getEcclesiaUid())
        .orElseThrow(() -> new NoSuchElementException("주보를 찾을 수 없습니다."));
    return toResponse(bulletin);
  }

  @Transactional
  public BulletinResponseDTO insert(List<MultipartFile> files, BulletinRequestDTO dto) {
    AccountAuthDTO account = requireManager();
    Long ecclesiaUid = account.getEcclesiaUid();

    List<MultipartFile> uploads = normalize(files);
    BulletinFilePolicy.validateSet(uploads.stream().map(BulletinService::kindOf).toList());
    validatePushGroups(ecclesiaUid, dto);
    if (bulletinRepository.existsByEcclesiaUidAndWorshipDate(ecclesiaUid, dto.getWorshipDate())) {
      throw new IllegalArgumentException("해당 날짜의 주보가 이미 있습니다. 수정해 주세요.");
    }

    storageGuard.reserve(ecclesiaUid, totalSize(uploads));
    Bulletin bulletin = bulletinRepository.save(Bulletin.builder()
        .ecclesiaUid(ecclesiaUid)
        .worshipDate(dto.getWorshipDate())
        .title(dto.getTitle())
        .openYn(dto.getOpenYn() == null ? Yn.Y.name() : Yn.of(dto.getOpenYn()).name())
        .build());
    upload(bulletin, uploads, account);
    sendPushIfRequested(bulletin, dto, account);
    log.info("주보 등록 - id:{}, ecclesiaUid:{}, worshipDate:{}", bulletin.getId(), ecclesiaUid,
        bulletin.getWorshipDate());
    return toResponse(bulletin);
  }

  @Transactional
  public BulletinResponseDTO update(List<MultipartFile> files, BulletinRequestDTO dto) {
    AccountAuthDTO account = requireManager();
    Long ecclesiaUid = account.getEcclesiaUid();
    if (dto.getId() == null) {
      throw new IllegalArgumentException("수정할 주보 id 가 필요합니다.");
    }
    Bulletin bulletin = bulletinRepository.findByIdAndEcclesiaUid(dto.getId(), ecclesiaUid)
        .orElseThrow(() -> new NoSuchElementException("주보를 찾을 수 없습니다."));

    // 삭제 대상은 이 주보의 파일만 인정한다 (다른 주보/공지의 파일 id 는 무시)
    List<FileDetails> active = activeDetails(bulletin);
    Set<Long> deleteIds = dto.getDeleteFileDetailIdList() == null ? Set.of()
        : Set.copyOf(dto.getDeleteFileDetailIdList());
    List<FileDetails> toDelete = active.stream().filter(d -> deleteIds.contains(d.getId())).toList();

    List<MultipartFile> uploads = normalize(files);
    List<Kind> remaining = new ArrayList<>();
    for (FileDetails d : active) {
      if (!deleteIds.contains(d.getId())) {
        Kind kind = BulletinFilePolicy.kindOfStored(d.getExtension());
        if (kind != null) {
          remaining.add(kind);
        }
      }
    }
    uploads.forEach(f -> remaining.add(kindOf(f)));
    BulletinFilePolicy.validateSet(remaining);
    validatePushGroups(ecclesiaUid, dto);

    if (!bulletin.getWorshipDate().equals(dto.getWorshipDate())
        && bulletinRepository.existsByEcclesiaUidAndWorshipDate(ecclesiaUid, dto.getWorshipDate())) {
      throw new IllegalArgumentException("해당 날짜의 주보가 이미 있습니다.");
    }

    storageGuard.reserve(ecclesiaUid, totalSize(uploads));
    bulletin.changeWorshipDate(dto.getWorshipDate());
    if (dto.getTitle() != null) {
      bulletin.changeTitle(dto.getTitle());
    }
    if (dto.getOpenYn() != null) {
      bulletin.changeOpenYn(Yn.of(dto.getOpenYn()));
    }
    upload(bulletin, uploads, account);

    long freed = 0L;
    for (FileDetails d : toDelete) {
      d.markAsDeleted();
      fileDetailsRepository.save(d);
      freed += d.getFileSize() != null ? d.getFileSize() : 0L;
    }
    storageGuard.release(ecclesiaUid, freed);
    sendPushIfRequested(bulletin, dto, account);
    return toResponse(bulletin);
  }

  @Transactional
  public void delete(Long id) {
    AccountAuthDTO account = requireManager();
    Bulletin bulletin = bulletinRepository.findByIdAndEcclesiaUid(id, account.getEcclesiaUid())
        .orElseThrow(() -> new NoSuchElementException("주보를 찾을 수 없습니다."));

    long freed = 0L;
    for (FileDetails d : activeDetails(bulletin)) {
      d.markAsDeleted();
      fileDetailsRepository.save(d);
      freed += d.getFileSize() != null ? d.getFileSize() : 0L;
    }
    storageGuard.release(account.getEcclesiaUid(), freed);
    bulletinRepository.delete(bulletin);
    log.info("주보 삭제 - id:{}, accountUid:{}", id, account.getUid());
  }

  /** 같은 교회의 관리자인지 확인하고, 교회 검증 기한이 지나지 않았는지 확인한다 */
  private AccountAuthDTO requireManager() {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    if (!AnnouncementAccessPolicy.canWriteEcclesia(account)) {
      throw new AccessDeniedException("주보를 관리할 권한이 없습니다.");
    }
    storageGuard.assertWritable(account.getEcclesiaUid());
    return account;
  }

  /** 알림 그룹이 내 교회의 그룹인지 저장 전에 확인한다 */
  private void validatePushGroups(Long ecclesiaUid, BulletinRequestDTO dto) {
    if ("Y".equals(dto.getPushNotificationSendYn())) {
      pushDispatchService.validateGroupIds(ecclesiaUid, dto.getPushGroupIds());
    }
  }

  private void sendPushIfRequested(Bulletin bulletin, BulletinRequestDTO dto,
      AccountAuthDTO account) {
    if (!"Y".equals(dto.getPushNotificationSendYn()) || Yn.N.name().equals(bulletin.getOpenYn())) {
      return;
    }
    pushDispatchService.send(PushRequest.builder()
        .sendAccountUid(account.getUid())
        .ecclesiaUid(account.getEcclesiaUid())
        .groupIds(dto.getPushGroupIds())
        .organization(OrganizationType.ECCLESIA.name())
        .category(CategoryType.BULLETIN.name())
        .title(BulletinPushMessage.TITLE)
        .message(BulletinPushMessage.message(bulletin.getTitle(), bulletin.getWorshipDate()))
        .data(BulletinPushMessage.routeData())
        .build());
  }

  private void upload(Bulletin bulletin, List<MultipartFile> uploads, AccountAuthDTO account) {
    if (uploads.isEmpty()) {
      return;
    }
    FileUploadResponseDTO response = fileService.uploadFileWithResponse(
        FileUploadWrapperDTO.builder()
            .fileId(bulletin.getFileUid())
            .categoryType(CategoryType.BULLETIN)
            .files(uploads)
            .accountAuth(account)
            .parentId(bulletin.getId())
            .build());
    bulletin.changeFileUid(response.getFileId());
  }

  /** 빈 파트는 버리고, 파일 종류를 서버에서 판별한 contentType 으로 고정한다 */
  private List<MultipartFile> normalize(List<MultipartFile> files) {
    if (files == null) {
      return List.of();
    }
    List<MultipartFile> result = new ArrayList<>();
    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        continue;
      }
      result.add(new NormalizedMultipartFile(file, contentTypeOf(file)));
    }
    return result;
  }

  private static Kind kindOf(MultipartFile file) {
    return BulletinFilePolicy.detect(file);
  }

  private static String contentTypeOf(MultipartFile file) {
    Kind kind = BulletinFilePolicy.detect(file);
    if (kind == Kind.PDF) {
      return "application/pdf";
    }
    String name = file.getOriginalFilename().toLowerCase();
    if (name.endsWith(".png")) {
      return "image/png";
    }
    if (name.endsWith(".webp")) {
      return "image/webp";
    }
    return "image/jpeg";
  }

  private static long totalSize(List<MultipartFile> files) {
    return files.stream().mapToLong(MultipartFile::getSize).sum();
  }

  private List<FileDetails> activeDetails(Bulletin bulletin) {
    if (bulletin.getFileUid() == null) {
      return List.of();
    }
    return fileDetailsRepository.findAllByFileIdAndDelYn(bulletin.getFileUid(), Yn.N.name());
  }

  private BulletinResponseDTO toResponse(Bulletin bulletin) {
    List<FileDetails> details = activeDetails(bulletin);
    List<BulletinFileDTO> fileList = details.stream()
        .map(d -> new BulletinFileDTO(d.getId(), d.getFileOriginalName(), d.getFileSize(),
            d.getExtension()))
        .collect(Collectors.toList());
    Kind kind = details.isEmpty() ? null : BulletinFilePolicy.kindOfStored(details.get(0).getExtension());
    String token = Optional.ofNullable(bulletin.getFileUid())
        .flatMap(uid -> fileRepository.findByIdAndDelYn(uid, Yn.N.name()))
        .map(f -> f.getAccessToken())
        .orElse(null);
    return BulletinResponseDTO.of(bulletin, kind == null ? null : kind.name(), token, fileList);
  }

  private static int clampLimit(Integer limit) {
    if (limit == null || limit < 1) {
      return DEFAULT_LIMIT;
    }
    return Math.min(limit, MAX_LIMIT);
  }
}
