package com.gospelee.api.service.gallery;

import static com.gospelee.api.utils.FileUtils.makeTodayPath;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.gallery.GalleryImageListRequestDTO;
import com.gospelee.api.dto.gallery.GalleryImageListResponseDTO;
import com.gospelee.api.dto.gallery.GalleryImageResponseDTO;
import com.gospelee.api.dto.gallery.StorageStatusResponseDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.entity.FileDetails;
import com.gospelee.api.entity.FileEntity;
import com.gospelee.api.entity.GalleryFolder;
import com.gospelee.api.entity.GalleryImage;
import com.gospelee.api.enums.CategoryType;
import com.gospelee.api.enums.ErrorResponseType;
import com.gospelee.api.enums.Yn;
import com.gospelee.api.exception.GalleryException;
import com.gospelee.api.repository.jpa.ecclesia.EcclesiaJpaRepository;
import com.gospelee.api.repository.jpa.file.FileDetailsRepository;
import com.gospelee.api.repository.jpa.file.FileRepository;
import com.gospelee.api.repository.jpa.gallery.GalleryFolderRepository;
import com.gospelee.api.repository.jpa.gallery.GalleryImageRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryImageServiceImpl implements GalleryImageService {

  private static final long MAX_FILE_SIZE = 20 * 1024 * 1024L; // 20MB
  private static final int MAX_UPLOAD_COUNT = 20;
  private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
      "jpg", "jpeg", "jfif", "png", "heic", "webp", "gif", "avif", "bmp"
  ));

  private final GalleryImageRepository galleryImageRepository;
  private final GalleryFolderRepository galleryFolderRepository;
  private final EcclesiaJpaRepository ecclesiaJpaRepository;
  private final FileRepository fileRepository;
  private final FileDetailsRepository fileDetailsRepository;

  @Value("${file.base-path}")
  private String fileBasePath;

  @Override
  @Transactional
  public List<GalleryImageResponseDTO> uploadImages(Long folderId, List<String> targetRoles,
      List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      throw new GalleryException(ErrorResponseType.COMM_101, "업로드할 파일이 없습니다.");
    }

    if (files.size() > MAX_UPLOAD_COUNT) {
      throw new GalleryException(ErrorResponseType.COMM_102, "한 번에 최대 20개의 사진만 업로드할 수 있습니다.");
    }

    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();
    if (ecclesiaUid == null) {
      throw new GalleryException(ErrorResponseType.ECCL_101);
    }

    Ecclesia ecclesia = ecclesiaJpaRepository.findEcclesiasByUid(ecclesiaUid)
        .orElseThrow(() -> new GalleryException(ErrorResponseType.ECCL_101));

    String effectiveTargetRolesStr = null;
    String folderName = null;

    if (folderId != null) {
      GalleryFolder folder = galleryFolderRepository.findByIdAndEcclesiaUidAndDelYn(
          folderId, ecclesiaUid, Yn.N.name())
          .orElseThrow(() -> new GalleryException(ErrorResponseType.GALL_106, "선택한 폴더를 찾을 수 없습니다."));

      folderName = folder.getName();
      // 폴더 내부 사진은 폴더 권한을 고정적으로 상속
      effectiveTargetRolesStr = folder.getTargetRoles();
    } else {
      // 루트 업로드인 경우 직접 지정한 targetRoles 사용
      if (targetRoles != null && !targetRoles.isEmpty()) {
        effectiveTargetRolesStr = String.join(",", targetRoles);
      }
    }

    // 1. 유효성 검사 (확장자, 개별 파일 크기, 전체 용량 계산)
    long totalIncomingBytes = 0L;
    for (MultipartFile file : files) {
      if (file.isEmpty()) {
        continue;
      }

      String originalFilename = file.getOriginalFilename();
      String extension = getExtension(originalFilename);

      if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
        throw new GalleryException(ErrorResponseType.GALL_103,
            "지원하지 않는 파일 형식입니다 (" + extension + "). 지원 형식: " + String.join(", ", ALLOWED_EXTENSIONS));
      }

      if (file.getSize() > MAX_FILE_SIZE) {
        throw new GalleryException(ErrorResponseType.GALL_102,
            "파일 크기가 20MB를 초과했습니다 (" + originalFilename + ")");
      }

      totalIncomingBytes += file.getSize();
    }

    // 2. 교회 용량 초과 여부 검사
    if (!ecclesia.hasEnoughStorage(totalIncomingBytes)) {
      throw new GalleryException(ErrorResponseType.GALL_101,
          "교회 저장 공간 용량(10GB)을 초과하여 더 이상 업로드할 수 없습니다.");
    }

    // 3. FileEntity 생성 (카테고리: GALLERY)
    FileEntity fileEntity = FileEntity.builder()
        .accountUid(user.getUid())
        .category(CategoryType.GALLERY.name())
        .parentId(String.valueOf(ecclesiaUid))
        .totalCount(files.size())
        .delYn(Yn.N.name())
        .accessToken(UUID.randomUUID().toString())
        .build();
    fileEntity = fileRepository.save(fileEntity);

    List<GalleryImageResponseDTO> responseList = new ArrayList<>();
    String relativeDirPath = user.getUid() + File.separator + CategoryType.GALLERY.lowerCaseName() + makeTodayPath();
    String fullDirPath = fileBasePath + File.separator + relativeDirPath;
    createDirectoryIfNotExists(fullDirPath);

    // 4. 각 파일 저장 및 썸네일 생성
    for (MultipartFile file : files) {
      if (file.isEmpty()) {
        continue;
      }

      String originalFilename = file.getOriginalFilename();
      String extension = getExtension(originalFilename);
      String uuid = UUID.randomUUID().toString();
      String origFileSaveName = uuid + "." + extension;
      String origFilePath = relativeDirPath + File.separator + origFileSaveName;
      File origDestFile = new File(fullDirPath, origFileSaveName);

      try {
        file.transferTo(origDestFile);
      } catch (IOException e) {
        log.error("원본 파일 저장 실패: {}", e.getMessage(), e);
        throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
      }

      // 원본 FileDetails 저장
      FileDetails origDetail = FileDetails.builder()
          .fileId(fileEntity.getId())
          .filePath(origFilePath)
          .fileOriginalName(originalFilename)
          .fileSize(file.getSize())
          .fileType(file.getContentType())
          .extension(extension)
          .delYn(Yn.N.name())
          .build();
      origDetail = fileDetailsRepository.save(origDetail);

      // 썸네일 생성 시도 (400x400)
      FileDetails thumbDetail = null;
      String thumbSaveName = "thumb_" + uuid + ".jpg";
      String thumbFilePath = relativeDirPath + File.separator + thumbSaveName;
      File thumbDestFile = new File(fullDirPath, thumbSaveName);

      try {
        Thumbnails.of(origDestFile)
            .size(400, 400)
            .outputFormat("jpg")
            .outputQuality(0.85)
            .toFile(thumbDestFile);

        thumbDetail = FileDetails.builder()
            .fileId(fileEntity.getId())
            .filePath(thumbFilePath)
            .fileOriginalName("thumb_" + originalFilename)
            .fileSize(thumbDestFile.length())
            .fileType("image/jpeg")
            .extension("jpg")
            .delYn(Yn.N.name())
            .build();
        thumbDetail = fileDetailsRepository.save(thumbDetail);
      } catch (Exception e) {
        log.warn("썸네일 생성 실패 (원본 이미지 사용): file={}, err={}", originalFilename, e.getMessage());
      }

      // GalleryImage Entity 생성
      GalleryImage galleryImage = GalleryImage.builder()
          .ecclesiaUid(ecclesiaUid)
          .folderId(folderId)
          .fileId(fileEntity.getId())
          .originalFileDetailId(origDetail.getId())
          .thumbnailFileDetailId(thumbDetail != null ? thumbDetail.getId() : origDetail.getId())
          .fileSize(file.getSize())
          .extension(extension)
          .targetRoles(effectiveTargetRolesStr)
          .delYn(Yn.N.name())
          .build();
      galleryImage = galleryImageRepository.save(galleryImage);

      String origUrl = "/file/" + fileEntity.getAccessToken() + "/" + origDetail.getId();
      String thumbUrl = "/file/" + fileEntity.getAccessToken() + "/"
          + (thumbDetail != null ? thumbDetail.getId() : origDetail.getId());

      responseList.add(GalleryImageResponseDTO.builder()
          .id(galleryImage.getId())
          .ecclesiaUid(ecclesiaUid)
          .folderId(folderId)
          .folderName(folderName)
          .fileId(fileEntity.getId())
          .originalFileDetailId(origDetail.getId())
          .thumbnailFileDetailId(thumbDetail != null ? thumbDetail.getId() : origDetail.getId())
          .originalUrl(origUrl)
          .thumbnailUrl(thumbUrl)
          .originalFileName(originalFilename)
          .fileSize(file.getSize())
          .extension(extension)
          .targetRoles(stringToRoles(effectiveTargetRolesStr))
          .insertTime(galleryImage.getInsertTime())
          .build());
    }

    // 5. 사용 용량 증가 반영
    ecclesia.addStorageUsedBytes(totalIncomingBytes);

    return responseList;
  }

  @Override
  @Transactional(readOnly = true)
  public GalleryImageListResponseDTO getImages(GalleryImageListRequestDTO request) {
    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();
    if (ecclesiaUid == null) {
      return GalleryImageListResponseDTO.builder()
          .content(Collections.emptyList())
          .totalElements(0)
          .totalPages(0)
          .page(0)
          .size(request.getSize() != null ? request.getSize() : 20)
          .build();
    }

    LocalDate startDate = null;
    LocalDate endDate = null;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    if (request.getStartDate() != null && !request.getStartDate().trim().isEmpty()) {
      try {
        startDate = LocalDate.parse(request.getStartDate().trim(), formatter);
      } catch (Exception ignored) {}
    }
    if (request.getEndDate() != null && !request.getEndDate().trim().isEmpty()) {
      try {
        endDate = LocalDate.parse(request.getEndDate().trim(), formatter);
      } catch (Exception ignored) {}
    }

    int page = request.getPage() != null ? Math.max(0, request.getPage()) : 0;
    int size = request.getSize() != null ? Math.max(1, request.getSize()) : 20;

    List<GalleryImage> images = galleryImageRepository.findImagesByCriteria(
        ecclesiaUid, request.getFolderId(), request.getIncludeUncategorized(),
        startDate, endDate, request.getTargetRole(), page, size);

    long totalElements = galleryImageRepository.countImagesByCriteria(
        ecclesiaUid, request.getFolderId(), request.getIncludeUncategorized(),
        startDate, endDate, request.getTargetRole());

    int totalPages = (int) Math.ceil((double) totalElements / size);

    // Folder Map & FileEntity Map 캐싱
    Set<Long> folderIds = images.stream().map(GalleryImage::getFolderId).filter(id -> id != null).collect(Collectors.toSet());
    Map<Long, String> folderNameMap = folderIds.isEmpty() ? Collections.emptyMap() :
        galleryFolderRepository.findAllById(folderIds).stream().collect(Collectors.toMap(GalleryFolder::getId, GalleryFolder::getName));

    Set<Long> fileIds = images.stream().map(GalleryImage::getFileId).collect(Collectors.toSet());
    Map<Long, String> fileTokenMap = fileIds.isEmpty() ? Collections.emptyMap() :
        fileRepository.findAllById(fileIds).stream().collect(Collectors.toMap(FileEntity::getId, FileEntity::getAccessToken));

    Set<Long> detailIds = new HashSet<>();
    images.forEach(img -> {
      detailIds.add(img.getOriginalFileDetailId());
      if (img.getThumbnailFileDetailId() != null) {
        detailIds.add(img.getThumbnailFileDetailId());
      }
    });
    Map<Long, FileDetails> detailsMap = detailIds.isEmpty() ? Collections.emptyMap() :
        fileDetailsRepository.findAllById(detailIds).stream().collect(Collectors.toMap(FileDetails::getId, d -> d));

    List<GalleryImageResponseDTO> content = images.stream().map(image -> {
      String token = fileTokenMap.getOrDefault(image.getFileId(), "");
      FileDetails orig = detailsMap.get(image.getOriginalFileDetailId());
      String origName = orig != null ? orig.getFileOriginalName() : "";
      String origUrl = "/file/" + token + "/" + image.getOriginalFileDetailId();
      String thumbUrl = "/file/" + token + "/" + (image.getThumbnailFileDetailId() != null ? image.getThumbnailFileDetailId() : image.getOriginalFileDetailId());

      return GalleryImageResponseDTO.builder()
          .id(image.getId())
          .ecclesiaUid(image.getEcclesiaUid())
          .folderId(image.getFolderId())
          .folderName(folderNameMap.get(image.getFolderId()))
          .fileId(image.getFileId())
          .originalFileDetailId(image.getOriginalFileDetailId())
          .thumbnailFileDetailId(image.getThumbnailFileDetailId())
          .originalUrl(origUrl)
          .thumbnailUrl(thumbUrl)
          .originalFileName(origName)
          .fileSize(image.getFileSize())
          .extension(image.getExtension())
          .targetRoles(stringToRoles(image.getTargetRoles()))
          .insertTime(image.getInsertTime())
          .build();
    }).collect(Collectors.toList());

    return GalleryImageListResponseDTO.builder()
        .content(content)
        .totalElements(totalElements)
        .totalPages(totalPages)
        .page(page)
        .size(size)
        .build();
  }

  @Override
  @Transactional
  public void deleteImages(List<Long> imageIds) {
    if (imageIds == null || imageIds.isEmpty()) {
      return;
    }

    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();

    List<GalleryImage> images = galleryImageRepository.findByIdInAndEcclesiaUidAndDelYn(
        imageIds, ecclesiaUid, Yn.N.name());

    long freedBytes = 0L;
    for (GalleryImage image : images) {
      image.markAsDeleted();
      freedBytes += (image.getFileSize() != null ? image.getFileSize() : 0L);
    }

    if (freedBytes > 0) {
      final long totalFreed = freedBytes;
      ecclesiaJpaRepository.findEcclesiasByUid(ecclesiaUid).ifPresent(ecclesia -> {
        ecclesia.subtractStorageUsedBytes(totalFreed);
      });
    }
  }

  @Override
  @Transactional(readOnly = true)
  public StorageStatusResponseDTO getStorageStatus() {
    AccountAuthDTO user = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    Long ecclesiaUid = user.getEcclesiaUid();
    if (ecclesiaUid == null) {
      throw new GalleryException(ErrorResponseType.ECCL_101);
    }

    Ecclesia ecclesia = ecclesiaJpaRepository.findEcclesiasByUid(ecclesiaUid)
        .orElseThrow(() -> new GalleryException(ErrorResponseType.ECCL_101));

    long limit = ecclesia.getStorageLimitBytesOrDefault();
    long used = ecclesia.getStorageUsedBytesOrDefault();
    double percent = limit > 0 ? ((double) used / limit) * 100.0 : 0.0;

    return StorageStatusResponseDTO.builder()
        .ecclesiaUid(ecclesia.getUid())
        .ecclesiaName(ecclesia.getName())
        .storageLimitBytes(limit)
        .storageUsedBytes(used)
        .usedPercentage(Math.round(percent * 10.0) / 10.0)
        .formattedLimit(formatBytes(limit))
        .formattedUsed(formatBytes(used))
        .build();
  }

  private String formatBytes(long bytes) {
    if (bytes >= 1024 * 1024 * 1024L) {
      return String.format("%.2f GB", (double) bytes / (1024 * 1024 * 1024L));
    } else if (bytes >= 1024 * 1024L) {
      return String.format("%.1f MB", (double) bytes / (1024 * 1024L));
    } else if (bytes >= 1024L) {
      return String.format("%.0f KB", (double) bytes / 1024L);
    }
    return bytes + " B";
  }

  private String getExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf(".") + 1);
  }

  private boolean createDirectoryIfNotExists(String pathStr) {
    Path path = Paths.get(pathStr);
    if (!Files.exists(path)) {
      try {
        Files.createDirectories(path);
        return true;
      } catch (IOException e) {
        log.error("디렉토리 생성 실패: {}", e.getMessage());
        return false;
      }
    }
    return true;
  }

  private List<String> stringToRoles(String rolesStr) {
    if (rolesStr == null || rolesStr.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.stream(rolesStr.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }
}
